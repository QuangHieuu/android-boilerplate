package it.cpc.vn.excel_to_pdf.helpers;

import static com.itextpdf.text.pdf.PdfDiv.BorderTopStyle.DASHED;
import static com.itextpdf.text.pdf.PdfDiv.BorderTopStyle.DOTTED;
import static com.itextpdf.text.pdf.PdfDiv.BorderTopStyle.DOUBLE;
import static org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT;
import static org.apache.poi.ss.usermodel.BorderStyle.DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.BorderStyle.HAIR;
import static org.apache.poi.ss.usermodel.BorderStyle.MEDIUM;
import static org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASHED;
import static org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT;
import static org.apache.poi.ss.usermodel.BorderStyle.MEDIUM_DASH_DOT_DOT;
import static org.apache.poi.ss.usermodel.BorderStyle.NONE;
import static org.apache.poi.ss.usermodel.BorderStyle.SLANTED_DASH_DOT;
import static org.apache.poi.ss.usermodel.BorderStyle.THICK;
import static org.apache.poi.ss.usermodel.BorderStyle.THIN;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.Formatter;
import java.util.Map;

import it.cpc.vn.excel_to_pdf.utils.ExcelUtils;

public interface HtmlHelper {

    String AUTO_COLOR = "#000";

    Map<BorderStyle, String> BORDER = ExcelUtils.mapFor(
        DASH_DOT, "%s dashed 1pt", DASH_DOT_DOT, "%s dashed 1pt",
        DASHED, "%s dashed 1pt", DOTTED, "%s dotted 1pt",
        DOUBLE, "%s double 3pt", HAIR, "%s solid 1px",
        MEDIUM, "%s solid 2pt", MEDIUM_DASH_DOT, "%s dashed 2pt",
        MEDIUM_DASH_DOT_DOT, "%s dashed 2pt", MEDIUM_DASHED, "%s dashed 2pt",
        NONE, "none", SLANTED_DASH_DOT, "%s dashed 2pt",
        THICK, "%s solid 3pt", THIN, "%s solid 1pt");

    void colorStyles(CellStyle style, Formatter out);

    void borderStyles(CellStyle style, Formatter out);

}
