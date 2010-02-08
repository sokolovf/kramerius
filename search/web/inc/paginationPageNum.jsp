<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.util.*, cz.incad.Solr.CzechComparator, cz.incad.Solr.*" %>
<%
// zaklad od kamila 
            String expandedPaginationStr = "";
            int numHits = Integer.parseInt((String) request.getAttribute("rows"));
            int numDocs = Integer.parseInt((String) request.getAttribute("numDocs"));
            String div = (String) request.getParameter("d");
            String filters = (String) request.getAttribute("filters");
            String offsetUrl = "";
            boolean includeAbeceda = false;
            Facet abecedaFacet = null;
            if(div==null){
                offsetUrl = "javascript:gotoPageOffset(%s);";
            }else{
                offsetUrl = "javascript:gotoPageOffsetInTree(%s, '"+div+"', '"+filters+"', '"+request.getParameter("pid")+"');";
                includeAbeceda = true;
                abecedaFacet = facets.get("abeceda_title");
                abecedaFacet.sortByName();
            }
            
            if (numDocs > numHits && numHits > 0) {
                String navigationPageTemplate = "";
                int N_WIDTH = 10;
                int offset = 0;
                if (request.getParameter("offset") != null) {
                    offset = Integer.parseInt((String) request.getParameter("offset"));
                }
                int maxPageIndex = Math.min(((numDocs - 1) / numHits + 1), 4000 / numHits);
                int pageIndex = offset / numHits;
                int nStart;
                int nEnd;
                if (pageIndex <= (N_WIDTH / 2) || maxPageIndex <= N_WIDTH) {
                    nStart = 1;
                } else {
                    nStart = pageIndex - (N_WIDTH / 2);
                }
                nEnd = nStart + N_WIDTH;
                if (nEnd > maxPageIndex) {
                    nEnd = maxPageIndex;
                    nStart = nEnd - N_WIDTH;
                    if (nStart < 1) {
                        nStart = 1;
                    }
                }
                StringBuffer navigationPages = new StringBuffer();
                if (nStart > 1) { // skok na zacatek
                    navigationPages.append("<a href='"+String.format(offsetUrl, "0")+"'>&laquo;</a> ");
                }
                /*
                 *             if (offset >= numHits) { // predesla strana
                navigationPages.append("<a href='javascript:gotoPageOffset(" +
                (offset - numHits) + ");'>&lt;</a>");
                }
                 */
                maxPageIndex = nEnd;
                String pismeno;
                for (pageIndex = nStart; pageIndex <= maxPageIndex; pageIndex++) {
                    navigationPages.append((pageIndex == nStart) ? "" : " ");
                    nStart = (pageIndex - 1) * numHits;
                    nEnd = (pageIndex) * numHits;
                    if (nEnd > numDocs) {
                        nEnd = numDocs;
                    }
                    if (nStart == offset) {
                        //navigationPages.append("<b>" + (nStart + 1) + "-" + nEnd + "</b>");
                        navigationPages.append("<b>" + pageIndex + "</b> ");
                    } else {
                        //navigationPages.append("<a href=\"javascript:gotoPageOffset(" + String.valueOf(nStart) + ");\">" + (nStart + 1) + "-" + nEnd + "</a>");
                        navigationPages.append("<a href=\""+String.format(offsetUrl, String.valueOf(nStart))+"\">" + pageIndex);
                        if(includeAbeceda){
                            pismeno = abecedaFacet.getDisplayNameByAcumulatedCount(nStart, numHits);
                            if (!pismeno.equals(""))
                            navigationPages.append(" (" + pismeno + ")");
                            //navigationPages.append(" [" + String.valueOf(nStart) + ", " + String.valueOf(numHits) + "] ");
                        }
                        navigationPages.append("</a> ");
                    }
                }
                if (offset < (numDocs - numHits)) { // dalsi strana
                    //navigationPages.append(" | <a href=\"").append(navigationPageTemplate.replace("INSERT_OFFSET", String.valueOf(offset + numHits))).append("\">&gt;</a>");
                    navigationPages.append("<a class=\"next\" href=\""+String.format(offsetUrl, (offset + numHits))+"\">&gt;&gt;</a> ");
                }
                expandedPaginationStr = navigationPages.toString();
            }
%>
<%=expandedPaginationStr%>