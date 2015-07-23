package module.fek.annotator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Christos Sardianos
 * @version 2.0
 */
public class RegexFEK {

    /**
     * Gets an Article object and searches for FEK entities into article body.
     *
     * @author Christos Sardianos
     * @param curArticle - The Article to be searched
     * @return The same Article object but with its list of Annotations filled.
     * @throws Exception
     */
    public ArticleForAnnotation FindFekRegex(ArticleForAnnotation curArticle) throws Exception {
        // Regular expression to be matched
//        Pattern FekPattern = Pattern.compile("((\\d+\\w{1,2}? +)?[νΝ]\\. ?\\d+/\\d+( *\\([Α-Ωα-ω]{1,2}[΄΄'’]? ?\\d+\\))?)", Pattern.UNICODE_CASE);
        // Matches Laws and POs (ΠΔ)
//        Pattern FekPattern = Pattern.compile("(((([Νν][όο]μ)[α-ω]{0,3}|[νΝ])\\.?|([Ππ]\\.?[Δδ]\\.?)) ?\\d+\\/\\d+( *\\((ΦΕΚ )?[Α-Ωα-ω]{1,2}[΄`΄'’]? ?\\d+((\\/|, ?)(\\d|\\.|-)*)?\\))?)", Pattern.UNICODE_CASE);
        Pattern FekPattern = Pattern.compile("(((άρθ[α-ω\\.]{0,} ?\\d+,? [Α-Ωα-ω0-9, ]*παρ[α-ωά\\.]{0,} ?\\d+,? [Α-Ωα-ω0-9, ]*)|(παρ[α-ωά\\.]{0,} ?\\d+,? [Α-Ωα-ω0-9, ]*άρθ[α-ω\\.]{0,4} ?\\d+,? [Α-Ωα-ω0-9, ]*))?((([Νν][όο]μ)[α-ω]{0,3}|[νΝ])\\.?|([Ππ]\\.?[Δδ]\\.?)) ?\\d+\\/\\d+( *\\((ΦΕΚ )?[Α-Ωα-ω]{1,2}[΄`΄'’]? ?\\d+((\\/|, ?)(\\d|\\.|-)*)?\\))?)", Pattern.UNICODE_CASE);
        Pattern LawPattern = Pattern.compile("((([Νν][όο]μ)[α-ω]{0,3}|[νΝ])\\.?|([Ππ]\\.?[Δδ]\\.?)) ?\\d+\\/\\d+", Pattern.UNICODE_CASE);
        // Start searching article text
        String articText = curArticle.articleText;
        Matcher m = FekPattern.matcher(articText);
        while (m.find()) {
            String entityFound = m.group(0);

            Matcher mLaw = LawPattern.matcher(entityFound);
            if (!mLaw.find()) {
                continue;
            }
            String entityLaw = mLaw.group(0);

            String[] FEKparts = entityFound.split("/", 2);
            int startIndex = m.start();
            int endIndex = m.end();
            String FEK_year = "";
            String FEK_issue = "";
            String FEK_number = null;
            String urlpdf = "";
            String entity_Type = "fek";

            // Get the second part from a FEK annotation (e.g. from ν. 2873/2000 (Α’ 285) the 2000 (Α’ 285) part)
            // and check if FEK issue and number exist (inside parenthesis)
            if (FEKparts[1].contains("(")) {
                // Check inside that part for mached accent characters (΄ or ' or ’)
                // and if so, we get from this string the FEK issue (e.g. A) and FEK number
                FEK_year = FEKparts[1].substring(0, FEKparts[1].lastIndexOf("(")).trim();
                if (FEKparts[1].contains("΄")) {
                    FEK_issue = FEKparts[1].substring(FEKparts[1].lastIndexOf("("), FEKparts[1].lastIndexOf("΄")).trim().replace("(", "");
                    FEK_number = FEKparts[1].substring(FEKparts[1].lastIndexOf("΄"), FEKparts[1].lastIndexOf(")")).replace("΄", "").trim();
                } else if (FEKparts[1].contains("'")) {
                    FEK_issue = FEKparts[1].substring(FEKparts[1].lastIndexOf("("), FEKparts[1].lastIndexOf("'")).trim().replace("(", "");
                    FEK_number = FEKparts[1].substring(FEKparts[1].lastIndexOf("'"), FEKparts[1].lastIndexOf(")")).replace("'", "").trim();
                } else if (FEKparts[1].contains("’")) {
                    FEK_issue = FEKparts[1].substring(FEKparts[1].lastIndexOf("("), FEKparts[1].lastIndexOf("’")).trim().replace("(", "");
                    FEK_number = FEKparts[1].substring(FEKparts[1].lastIndexOf("’"), FEKparts[1].lastIndexOf(")")).replace("’", "").trim();
                } else if (FEKparts[1].contains("`")) {
                    FEK_issue = FEKparts[1].substring(FEKparts[1].lastIndexOf("("), FEKparts[1].lastIndexOf("`")).trim().replace("(", "");
                    FEK_number = FEKparts[1].substring(FEKparts[1].lastIndexOf("`"), FEKparts[1].lastIndexOf(")")).replace("`", "").trim();
                } else {
                    // If doesn't contain on of these characters then we check for space
                    // else we get the first character as the FEK issue (e.g. A, B etc)
                    FEK_issue = FEKparts[1].substring(FEKparts[1].lastIndexOf("("), FEKparts[1].lastIndexOf(")")).trim().replace("(", "").replace(")", "");
                    if (FEK_issue.startsWith("ΦΕΚ ")) {
                        FEK_issue = FEK_issue.substring(4);
                    } else if (FEK_issue.startsWith("ΦΕΚ")) {
                        FEK_issue = FEK_issue.substring(3);
                    }

                    if (FEK_issue.contains(" ")) {
                        String[] splitedIssue = FEK_issue.split(" ");
                        FEK_issue = splitedIssue[0];
                        FEK_number = splitedIssue[1];
                    } else {
                        FEK_number = FEK_issue.substring(1);
                        FEK_issue = FEK_issue.substring(0, 1);
                    }
                }
                if (FEK_issue.startsWith("ΦΕΚ ")) {
                    FEK_issue = FEK_issue.substring(4);
                }
                String[] FEK_num_tokens = FEK_number.split("(\\/|-| |,)");

                FEK_number = FEK_num_tokens[0];

                // Set the corresponding issue checkbox based on the FEK issue retrieved above
                String checkBoxIssue = "";
                switch (FEK_issue) {
                    case "Α":
                        checkBoxIssue = "chbIssue_1";
                        break;
                    case "Β":
                        checkBoxIssue = "chbIssue_2";
                        break;
                    case "Γ":
                        checkBoxIssue = "chbIssue_3";
                        break;
                    case "Δ":
                        checkBoxIssue = "chbIssue_4";
                        break;
                    case "":
                        checkBoxIssue = "";
                        break;
                }

                // Perform the POST request
                PostRequestFEK httpRequest = new PostRequestFEK();
                // Get the corresponding pdf url of the FEK
                urlpdf = httpRequest.sendPost3(FEK_year, FEK_issue, FEK_number, checkBoxIssue);
//                if (urlpdf.length() == 0) {
//                    urlpdf = "-1";
//                }
            } else {
                FEK_year = FEKparts[1];
                String[] toks = entityLaw.split("(\\.|,| |\\/)");
                if (entityLaw.toLowerCase().startsWith("ν")) {
                    entity_Type = "law";
                } else if (entityLaw.toLowerCase().startsWith("π")) {
                    entity_Type = "pd";
                }
                String item_number = toks[toks.length - 2];
                PostRequestFEK httpRequest = new PostRequestFEK();
                urlpdf = httpRequest.sendPost4(FEK_year, item_number, entity_Type);
            }
            // Add the current annotation found into the current article's annotations list
            Annotation currentAnnotation = new Annotation(entityFound, startIndex, endIndex, urlpdf, entity_Type, entityLaw);
            curArticle.annotations.add(currentAnnotation);
        }
        // Return the article back to the process
        return curArticle;
    }

}
