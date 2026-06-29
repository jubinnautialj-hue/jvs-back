package cn.bctools.word.utils;

import cn.bctools.common.exception.BusinessException;
import cn.hutool.http.HttpUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.TextUtils;
import org.docx4j.XmlUtils;
import org.docx4j.convert.in.xhtml.XHTMLImporterImpl;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.table.tr.TrHeight;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.*;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPrBase.Ind;
import org.docx4j.wml.PPrBase.PBdr;
import org.docx4j.wml.PPrBase.Spacing;
import org.docx4j.wml.SectPr.PgBorders;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.SectPr.Type;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.HMerge;
import org.docx4j.wml.TcPrInner.VMerge;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author auto
 */
@Slf4j
public class Docx4jUtil {

    /**
     * @Description:新增超链接
     */
    public static void createHyperlink(WordprocessingMLPackage mlPackage, MainDocumentPart mainPart,
                                       ObjectFactory factory, P paragraph, String url, String value, String cnFontName, String enFontName,
                                       String fontSize) throws Exception {
        if (StringUtils.isBlank(enFontName)) {
            enFontName = "Times New Roman";
        }
        if (StringUtils.isBlank(cnFontName)) {
            cnFontName = "Microsoft YaHei";
        }
        if (StringUtils.isBlank(fontSize)) {
            fontSize = "22";
        }
        org.docx4j.relationships.ObjectFactory reFactory = new org.docx4j.relationships.ObjectFactory();
        Relationship rel = reFactory.createRelationship();
        rel.setType(Namespaces.HYPERLINK);
        rel.setTarget(url);
        rel.setTargetMode("External");
        mainPart.getRelationshipsPart().addRelationship(rel);
        StringBuffer sb = new StringBuffer();
        // addRelationship sets the rel's @Id
        sb.append("<w:hyperlink r:id=\"");
        sb.append(rel.getId());
        sb.append("\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >");
        sb.append("<w:r><w:rPr><w:rStyle w:val=\"Hyperlink\" />");
        sb.append("<w:rFonts  w:ascii=\"");
        sb.append(enFontName);
        sb.append("\"  w:hAnsi=\"");
        sb.append(enFontName);
        sb.append("\"  w:eastAsia=\"");
        sb.append(cnFontName);
        sb.append("\" w:hint=\"eastAsia\"/>");
        sb.append("<w:sz w:val=\"");
        sb.append(fontSize);
        sb.append("\"/><w:szCs w:val=\"");
        sb.append(fontSize);
        sb.append("\"/></w:rPr><w:t>");
        sb.append(value);
        sb.append("</w:t></w:r></w:hyperlink>");

        Hyperlink link = (Hyperlink) XmlUtils.unmarshalString(sb.toString());
        paragraph.getContent().add(link);
    }

    public static String getElementContent(Object obj) throws Exception {
        StringWriter stringWriter = new StringWriter();
        TextUtils.extractText(obj, stringWriter);
        return stringWriter.toString();
    }

    /**
     * @Description:得到指定类型的元素
     */
    public static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<Object>();
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement<?>) obj).getValue();
        }
        if (obj.getClass().equals(toSearch)) {
            result.add(obj);
        } else if (obj instanceof ContentAccessor) {
            List<?> children = ((ContentAccessor) obj).getContent();
            for (Object child : children) {
                result.addAll(getAllElementFromObject(child, toSearch));
            }
        }
        return result;
    }

    /**
     * @Description:保存WordprocessingMLPackage
     */
    public static void saveWordPackage(WordprocessingMLPackage wordPackage, File file) throws Exception {
        wordPackage.save(file);
    }

    /**
     * @Description:新建WordprocessingMLPackage
     */
    public static WordprocessingMLPackage createWordProcessingGmlPackage() throws Exception {
        return WordprocessingMLPackage.createPackage();
    }

    /**
     * @Description:加载带密码WordprocessingMLPackage
     */
    public static WordprocessingMLPackage loadWordProcessingMlPackageWithPwd(String filePath, String password)
            throws Exception {
        OpcPackage opcPackage = WordprocessingMLPackage.load(new File(filePath), password);
        WordprocessingMLPackage mlPackage = (WordprocessingMLPackage) opcPackage;
        return mlPackage;
    }

    /**
     * @Description:加载WordprocessingMLPackage
     */
    public static WordprocessingMLPackage loadWordProcessingMlPackage(String filePath) throws Exception {
        return WordprocessingMLPackage.load(new File(filePath));
    }

    /*------------------------------------Word 表格相关---------------------------------------------------  */

    /**
     * @Description: 跨列合并
     */
    public static void mergeCellsHorizontalByGridSpan(Tbl tbl, int row, int fromCell, int toCell) {
        if (row < 0 || fromCell < 0 || toCell < 0) {
            return;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row > trList.size()) {
            return;
        }
        Tr tr = trList.get(row);
        List<Tc> tcList = getTrAllCell(tr);
        for (int cellIndex = Math.min(tcList.size() - 1, toCell); cellIndex >= fromCell; cellIndex--) {
            Tc tc = tcList.get(cellIndex);
            TcPr tcPr = getTcPr(tc);
            if (cellIndex == fromCell) {
                GridSpan gridSpan = tcPr.getGridSpan();
                if (gridSpan == null) {
                    gridSpan = new GridSpan();
                    tcPr.setGridSpan(gridSpan);
                }
                gridSpan.setVal(BigInteger.valueOf(Math.min(tcList.size() - 1, toCell) - fromCell + 1));
            } else {
                tr.getContent().remove(cellIndex);
            }
        }
    }

    /**
     * @Description: 跨列合并
     */
    public static void mergeCellsHorizontal(Tbl tbl, int row, int fromCell, int toCell) {
        if (row < 0 || fromCell < 0 || toCell < 0) {
            return;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row > trList.size()) {
            return;
        }
        Tr tr = trList.get(row);
        List<Tc> tcList = getTrAllCell(tr);
        for (int cellIndex = fromCell, len = Math.min(tcList.size() - 1, toCell); cellIndex <= len; cellIndex++) {
            Tc tc = tcList.get(cellIndex);
            TcPr tcPr = getTcPr(tc);
            HMerge hMerge = tcPr.getHMerge();
            if (hMerge == null) {
                hMerge = new HMerge();
                tcPr.setHMerge(hMerge);
            }
            if (cellIndex == fromCell) {
                hMerge.setVal("restart");
            } else {
                hMerge.setVal("continue");
            }
        }
    }

    /**
     * @Description: 跨行合并
     */
    public static void mergeCellsVertically(Tbl tbl, int col, int fromRow, int toRow) {
        if (col < 0 || fromRow < 0 || toRow < 0) {
            return;
        }
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            Tc tc = getTc(tbl, rowIndex, col);
            if (tc == null) {
                break;
            }
            TcPr tcPr = getTcPr(tc);
            VMerge vMerge = tcPr.getVMerge();
            if (vMerge == null) {
                vMerge = new VMerge();
                tcPr.setVMerge(vMerge);
            }
            if (rowIndex == fromRow) {
                vMerge.setVal("restart");
            } else {
                vMerge.setVal("continue");
            }
        }
    }

    /**
     * @Description:得到指定位置的单元格
     */
    public static Tc getTc(Tbl tbl, int row, int cell) {
        if (row < 0 || cell < 0) {
            return null;
        }
        List<Tr> trList = getTblAllTr(tbl);
        if (row >= trList.size()) {
            return null;
        }
        List<Tc> tcList = getTrAllCell(trList.get(row));
        if (cell >= tcList.size()) {
            return null;
        }
        return tcList.get(cell);
    }

    /**
     * @Description:得到所有表格
     */
    public static List<Tbl> getAllTbl(WordprocessingMLPackage mlPackage) {
        MainDocumentPart mainDocPart = mlPackage.getMainDocumentPart();
        List<Object> objList = getAllElementFromObject(mainDocPart, Tbl.class);
        if (objList == null) {
            return null;
        }
        List<Tbl> tblList = new ArrayList<Tbl>();
        for (Object obj : objList) {
            if (obj instanceof Tbl) {
                Tbl tbl = (Tbl) obj;
                tblList.add(tbl);
            }
        }
        return tblList;
    }

    /**
     * @Description:删除指定位置的表格,删除后表格数量减一
     */
    public static boolean removeTableByIndex(WordprocessingMLPackage mlPackage, int index) throws Exception {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = mlPackage.getMainDocumentPart().getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            Object obj = XmlUtils.unwrap(objList.get(i));
            if (obj instanceof Tbl) {
                k++;
                if (k == index) {
                    mlPackage.getMainDocumentPart().getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * @Description: 获取单元格内容, 无分割符
     */
    public static String getTblContentStr(Tbl tbl) throws Exception {
        return getElementContent(tbl);
    }

    /**
     * @Description: 获取表格内容
     */
    public static List<String> getTblContentList(Tbl tbl) throws Exception {
        List<String> resultList = new ArrayList<String>();
        List<Tr> trList = getTblAllTr(tbl);
        for (Tr tr : trList) {
            StringBuffer sb = new StringBuffer();
            List<Tc> tcList = getTrAllCell(tr);
            for (Tc tc : tcList) {
                sb.append(getElementContent(tc) + ",");
            }
            resultList.add(sb.toString());
        }
        return resultList;
    }

    public static TblPr getTblPr(Tbl tbl) {
        TblPr tblPr = tbl.getTblPr();
        if (tblPr == null) {
            tblPr = new TblPr();
            tbl.setTblPr(tblPr);
        }
        return tblPr;
    }

    /**
     * @Description: 设置表格总宽度
     */
    public static void setTableWidth(Tbl tbl, String width) {
        if (StringUtils.isNotBlank(width)) {
            TblPr tblPr = getTblPr(tbl);
            TblWidth tblW = tblPr.getTblW();
            if (tblW == null) {
                tblW = new TblWidth();
                tblPr.setTblW(tblW);
            }
            tblW.setW(new BigInteger(width));
            tblW.setType("dxa");
        }
    }

    /**
     * @Description:创建表格(默认水平居中,垂直居中)
     */
    public static Tbl createTable(WordprocessingMLPackage wordPackage, int rowNum, int colsNum) throws Exception {
        colsNum = Math.max(1, colsNum);
        rowNum = Math.max(1, rowNum);
        int widthTwips = getWritableWidth(wordPackage);
        int colWidth = widthTwips / colsNum;
        log.info("创建表格,{}行,{}列,总宽度:{},列宽:{}", rowNum, colsNum, widthTwips, colWidth);
//        int colWidth = 250;
        int[] widthArr = new int[colsNum];
        Arrays.fill(widthArr, colWidth);
        return createTable(rowNum, colsNum, widthArr);
    }

    /**
     * @Description:创建表格(默认水平居中,垂直居中)
     */
    public static Tbl createTable(int rowNum, int colsNum, int[] widthArr) throws Exception {
        colsNum = Math.max(1, Math.min(colsNum, widthArr.length));
        rowNum = Math.max(1, rowNum);
        Tbl tbl = new Tbl();
        StringBuffer tblSb = new StringBuffer();
        tblSb.append("<w:tblPr ").append(Namespaces.W_NAMESPACE_DECLARATION).append(">");
        tblSb.append("<w:tblStyle w:val=\"TableGrid\"/>");
        tblSb.append("<w:tblW w:w=\"0\" w:type=\"auto\"/>");
        // 上边框
        tblSb.append("<w:tblBorders>");
        tblSb.append("<w:top w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 左边框
        tblSb.append("<w:left w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 下边框
        tblSb.append("<w:bottom w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        // 右边框
        tblSb.append("<w:right w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("<w:insideH w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("<w:insideV w:val=\"single\" w:sz=\"1\" w:space=\"0\" w:color=\"auto\"/>");
        tblSb.append("</w:tblBorders>");
        tblSb.append("</w:tblPr>");
        TblPr tblPr = null;
        tblPr = (TblPr) XmlUtils.unmarshalString(tblSb.toString());
        Jc jc = new Jc();
        // 单元格居中对齐
        jc.setVal(JcEnumeration.CENTER);
        tblPr.setJc(jc);

        tbl.setTblPr(tblPr);

        // 设定各单元格宽度
        TblGrid tblGrid = new TblGrid();
        tbl.setTblGrid(tblGrid);
        for (int i = 0; i < colsNum; i++) {
            TblGridCol gridCol = new TblGridCol();
            gridCol.setW(BigInteger.valueOf(widthArr[i]));
            tblGrid.getGridCol().add(gridCol);
        }
        // 新增行
        for (int j = 0; j < rowNum; j++) {
            Tr tr = new Tr();
            tbl.getContent().add(tr);
            // 列
            for (int i = 0; i < colsNum; i++) {
                Tc tc = new Tc();
                tr.getContent().add(tc);

                TcPr tcPr = new TcPr();
                TblWidth cellWidth = new TblWidth();
                cellWidth.setType("dxa");
                cellWidth.setW(BigInteger.valueOf(widthArr[i]));
                tcPr.setTcW(cellWidth);
                tc.setTcPr(tcPr);

                // 垂直居中
                setTcValign(tc, STVerticalJc.CENTER);
                P p = new P();
                PPr pPr = new PPr();
                pPr.setJc(jc);
                p.setPPr(pPr);
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
            }
        }
        return tbl;
    }

    /**
     * @Description:表格增加边框 可以设置上下左右四个边框样式以及横竖水平线样式
     */
    public static void setTblBorders(TblPr tblPr, CTBorder topBorder, CTBorder rightBorder, CTBorder bottomBorder,
                                     CTBorder leftBorder, CTBorder hBorder, CTBorder vBorder) {
        TblBorders borders = tblPr.getTblBorders();
        if (borders == null) {
            borders = new TblBorders();
            tblPr.setTblBorders(borders);
        }
        if (topBorder != null) {
            borders.setTop(topBorder);
        }
        if (rightBorder != null) {
            borders.setRight(rightBorder);
        }
        if (bottomBorder != null) {
            borders.setBottom(bottomBorder);
        }
        if (leftBorder != null) {
            borders.setLeft(leftBorder);
        }
        if (hBorder != null) {
            borders.setInsideH(hBorder);
        }
        if (vBorder != null) {
            borders.setInsideV(vBorder);
        }
    }

    /**
     * @Description: 设置表格水平对齐方式(仅对表格起作用, 单元格不一定水平对齐)
     */
    public static void setTblJcAlign(Tbl tbl, JcEnumeration jcType) {
        if (jcType != null) {
            TblPr tblPr = getTblPr(tbl);
            Jc jc = tblPr.getJc();
            if (jc == null) {
                jc = new Jc();
                tblPr.setJc(jc);
            }
            jc.setVal(jcType);
        }
    }

    /**
     * @Description: 设置表格水平对齐方式(包括单元格), 只对该方法前面产生的单元格起作用
     */
    public static void setTblAllJcAlign(Tbl tbl, JcEnumeration jcType) {
        if (jcType != null) {
            setTblJcAlign(tbl, jcType);
            List<Tr> trList = getTblAllTr(tbl);
            for (Tr tr : trList) {
                List<Tc> tcList = getTrAllCell(tr);
                for (Tc tc : tcList) {
                    setTcJcAlign(tc, jcType);
                }
            }
        }
    }

    /**
     * @Description: 设置表格垂直对齐方式(包括单元格), 只对该方法前面产生的单元格起作用
     */
    public static void setTblAllValign(Tbl tbl, STVerticalJc vAlignType) {
        if (vAlignType != null) {
            List<Tr> trList = getTblAllTr(tbl);
            for (Tr tr : trList) {
                List<Tc> tcList = getTrAllCell(tr);
                for (Tc tc : tcList) {
                    setTcValign(tc, vAlignType);
                }
            }
        }
    }

    /**
     * @Description: 设置单元格Margin
     */
    public static void setTableCellMargin(Tbl tbl, String top, String right, String bottom, String left) {
        TblPr tblPr = getTblPr(tbl);
        CTTblCellMar cellMar = tblPr.getTblCellMar();
        if (cellMar == null) {
            cellMar = new CTTblCellMar();
            tblPr.setTblCellMar(cellMar);
        }
        if (StringUtils.isNotBlank(top)) {
            TblWidth topW = new TblWidth();
            topW.setW(new BigInteger(top));
            topW.setType("dxa");
            cellMar.setTop(topW);
        }
        if (StringUtils.isNotBlank(right)) {
            TblWidth rightW = new TblWidth();
            rightW.setW(new BigInteger(right));
            rightW.setType("dxa");
            cellMar.setRight(rightW);
        }
        if (StringUtils.isNotBlank(bottom)) {
            TblWidth btW = new TblWidth();
            btW.setW(new BigInteger(bottom));
            btW.setType("dxa");
            cellMar.setBottom(btW);
        }
        if (StringUtils.isNotBlank(left)) {
            TblWidth leftW = new TblWidth();
            leftW.setW(new BigInteger(left));
            leftW.setType("dxa");
            cellMar.setLeft(leftW);
        }
    }

    /**
     * @Description: 得到表格所有的行
     */
    public static List<Tr> getTblAllTr(Tbl tbl) {
        List<Object> objList = getAllElementFromObject(tbl, Tr.class);
        List<Tr> trList = new ArrayList<Tr>();
        if (objList == null) {
            return trList;
        }
        for (Object obj : objList) {
            if (obj instanceof Tr) {
                Tr tr = (Tr) obj;
                trList.add(tr);
            }
        }
        return trList;

    }

    /**
     * @Description:设置tr高度
     */
    public static void setTrHeight(Tr tr, String heigth) {
        TrPr trPr = getTrPr(tr);
        CTHeight ctHeight = new CTHeight();
        ctHeight.setVal(new BigInteger(heigth));
        TrHeight trHeight = new TrHeight(ctHeight);
        trHeight.set(trPr);
    }

    /**
     * @Description: 在表格指定位置新增一行, 默认居中
     */
    public static void addTrByIndex(Tbl tbl, int index) {
        addTrByIndex(tbl, index, STVerticalJc.CENTER, JcEnumeration.CENTER);
    }

    /**
     * @Description: 在表格指定位置新增一行(默认按表格定义的列数添加)
     */
    public static void addTrByIndex(Tbl tbl, int index, STVerticalJc vAlign, JcEnumeration hAlign) {
        TblGrid tblGrid = tbl.getTblGrid();
        Tr tr = new Tr();
        if (tblGrid != null) {
            List<TblGridCol> gridList = tblGrid.getGridCol();
            for (TblGridCol tblGridCol : gridList) {
                Tc tc = new Tc();
                setTcWidth(tc, tblGridCol.getW().toString());
                if (vAlign != null) {
                    // 垂直居中
                    setTcValign(tc, vAlign);
                }
                P p = new P();
                if (hAlign != null) {
                    PPr pPr = new PPr();
                    Jc jc = new Jc();
                    // 单元格居中对齐
                    jc.setVal(hAlign);
                    pPr.setJc(jc);
                    p.setPPr(pPr);
                }
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
                tr.getContent().add(tc);
            }
        } else {
            // 大部分情况都不会走到这一步
            Tr firstTr = getTblAllTr(tbl).get(0);
            int cellSize = getTcCellSizeWithMergeNum(firstTr);
            for (int i = 0; i < cellSize; i++) {
                Tc tc = new Tc();
                if (vAlign != null) {
                    // 垂直居中
                    setTcValign(tc, vAlign);
                }
                P p = new P();
                if (hAlign != null) {
                    PPr pPr = new PPr();
                    Jc jc = new Jc();
                    // 单元格居中对齐
                    jc.setVal(hAlign);
                    pPr.setJc(jc);
                    p.setPPr(pPr);
                }
                R run = new R();
                p.getContent().add(run);
                tc.getContent().add(p);
                tr.getContent().add(tc);
            }
        }
        if (index >= 0 && index < tbl.getContent().size()) {
            tbl.getContent().add(index, tr);
        } else {
            tbl.getContent().add(tr);
        }
    }

    /**
     * @Description: 得到行的列数
     */
    public static int getTcCellSizeWithMergeNum(Tr tr) {
        int cellSize = 1;
        List<Tc> tcList = getTrAllCell(tr);
        if (tcList == null || tcList.size() == 0) {
            return cellSize;
        }
        cellSize = tcList.size();
        for (Tc tc : tcList) {
            TcPr tcPr = getTcPr(tc);
            GridSpan gridSpan = tcPr.getGridSpan();
            if (gridSpan != null) {
                cellSize += gridSpan.getVal().intValue() - 1;
            }
        }
        return cellSize;
    }

    /**
     * @Description: 删除指定行 删除后行数减一
     */
    public static boolean removeTrByIndex(Tbl tbl, int index) {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = tbl.getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            Object obj = XmlUtils.unwrap(objList.get(i));
            if (obj instanceof Tr) {
                k++;
                if (k == index) {
                    tbl.getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    public static TrPr getTrPr(Tr tr) {
        TrPr trPr = tr.getTrPr();
        if (trPr == null) {
            trPr = new TrPr();
            tr.setTrPr(trPr);
        }
        return trPr;
    }

    /**
     * @Description:隐藏行(只对表格中间的部分起作用,不包括首尾行)
     */
    public static void setTrHidden(Tr tr, boolean hidden) {
        List<Tc> tcList = getTrAllCell(tr);
        for (Tc tc : tcList) {
            setTcHidden(tc, hidden);
        }
    }

    /**
     * @Description: 设置单元格宽度
     */
    public static void setTcWidth(Tc tc, String width) {
        if (StringUtils.isNotBlank(width)) {
            TcPr tcPr = getTcPr(tc);
            TblWidth tcW = tcPr.getTcW();
            if (tcW == null) {
                tcW = new TblWidth();
                tcPr.setTcW(tcW);
            }
            tcW.setW(new BigInteger(width));
            tcW.setType("dxa");
        }
    }

    /**
     * @Description: 隐藏单元格内容
     */
    public static void setTcHidden(Tc tc, boolean hidden) {
        List<P> pList = getTcAllP(tc);
        for (P p : pList) {
            PPr ppr = getPpr(p);
            List<Object> list = getAllElementFromObject(p, R.class);
            if (list == null) {
                continue;
            }
            for (Object objR : list) {
                if (objR instanceof R) {
                    R r = (R) objR;
                    RPr rpr = getRpr(r);
                    setRprVanishStyle(rpr, hidden);
                }
            }
            setParaVanish(ppr, hidden);
        }
    }

    public static List<P> getTcAllP(Tc tc) {
        List<Object> objList = getAllElementFromObject(tc, P.class);
        List<P> pList = new ArrayList<P>();
        if (objList == null) {
            return pList;
        }
        for (Object obj : objList) {
            if (obj instanceof P) {
                P p = (P) obj;
                pList.add(p);
            }
        }
        return pList;
    }

    public static TcPr getTcPr(Tc tc) {
        TcPr tcPr = tc.getTcPr();
        if (tcPr == null) {
            tcPr = new TcPr();
            tc.setTcPr(tcPr);
        }
        return tcPr;
    }

    /**
     * @Description: 设置单元格垂直对齐方式
     */
    public static void setTcValign(Tc tc, STVerticalJc vAlignType) {
        if (vAlignType != null) {
            TcPr tcPr = getTcPr(tc);
            CTVerticalJc vAlign = new CTVerticalJc();
            vAlign.setVal(vAlignType);
            tcPr.setVAlign(vAlign);
        }
    }

    /**
     * @Description: 设置单元格水平对齐方式
     */
    public static void setTcJcAlign(Tc tc, JcEnumeration jcType) {
        if (jcType != null) {
            List<P> pList = getTcAllP(tc);
            for (P p : pList) {
                setParaJcAlign(p, jcType);
            }
        }
    }

    public static RPr getRpr(R r) {
        RPr rpr = r.getRPr();
        if (rpr == null) {
            rpr = new RPr();
            r.setRPr(rpr);
        }
        return rpr;
    }

    /**
     * @Description: 获取所有的单元格
     */
    public static List<Tc> getTrAllCell(Tr tr) {
        List<Object> objList = getAllElementFromObject(tr, Tc.class);
        List<Tc> tcList = new ArrayList<Tc>();
        if (objList == null) {
            return tcList;
        }
        for (Object tcObj : objList) {
            if (tcObj instanceof Tc) {
                Tc objTc = (Tc) tcObj;
                tcList.add(objTc);
            }
        }
        return tcList;
    }

    /**
     * @Description: 获取单元格内容
     */
    public static String getTcContent(Tc tc) throws Exception {
        return getElementContent(tc);
    }

    /**
     * @Description:设置单元格内容,content为null则清除单元格内容
     */
    public static void setTcContent(Tc tc, RPr rpr, String content) {
        List<Object> pList = tc.getContent();
        P p = null;
        if (pList != null && pList.size() > 0) {
            if (pList.get(0) instanceof P) {
                p = (P) pList.get(0);
            }
        } else {
            p = new P();
            tc.getContent().add(p);
        }
        R run = null;
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
        run = new R();
        p.getContent().add(run);
        if (content != null) {
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(rpr);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                // 换行
                run.getContent().add(br);
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(rpr);
                run.getContent().add(text);
            }
        }
    }

    /**
     * @Description:设置单元格内容,content为null则清除单元格内容 支持设置字体
     */
    public static void setTcContent(Tc tc, String content, String cnFonts, String enFonts) {
        ObjectFactory factory = Context.getWmlObjectFactory();
        RPr rpr = factory.createRPr();
        setFontFamily(rpr, cnFonts, enFonts);
        if (StringUtils.isNotBlank(cnFonts) || StringUtils.isNotBlank(enFonts)) {
            RFonts rf = rpr.getRFonts();
            if (rf == null) {
                rf = new RFonts();
                rpr.setRFonts(rf);
            }
            if (cnFonts != null) {
                rf.setEastAsia(cnFonts);
            }
            if (enFonts != null) {
                rf.setAscii(enFonts);
            }
        }
        List<Object> pList = tc.getContent();
        P p = null;
        if (pList != null && pList.size() > 0) {
            if (pList.get(0) instanceof P) {
                p = (P) pList.get(0);
            }
        } else {
            p = new P();
            tc.getContent().add(p);
        }
        R run = null;
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
        run = new R();
        p.getContent().add(run);
        if (content != null) {
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(rpr);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                // 换行
                run.getContent().add(br);
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(rpr);
                run.getContent().add(text);
            }
        }
    }

    /**
     * @Description:设置单元格内容,content为null则清除单元格内容
     */
    public static void removeTcContent(Tc tc) {
        List<Object> pList = tc.getContent();
        P p = null;
        if (pList != null && pList.size() > 0) {
            if (pList.get(0) instanceof P) {
                p = (P) pList.get(0);
            }
        } else {
            return;
        }
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
    }

    /**
     * @Description:删除指定位置的表格
     * @deprecated
     */
    public static void deleteTableByIndex2(WordprocessingMLPackage mlPackage, int index) throws Exception {
        if (index < 0) {
            return;
        }
        final String xpath = "(//w:tbl)[" + index + "]";
        final List<Object> jaxbNodes = mlPackage.getMainDocumentPart().getJAXBNodesViaXPath(xpath, true);
        if (jaxbNodes != null && jaxbNodes.size() > 0) {
            mlPackage.getMainDocumentPart().getContent().remove(jaxbNodes.get(0));
        }
    }

    /**
     * @Description:获取NodeList
     * @deprecated
     */
    public static List<Object> getObjectByXpath(WordprocessingMLPackage mlPackage, String xpath) throws Exception {
        final List<Object> jaxbNodes = mlPackage.getMainDocumentPart().getJAXBNodesViaXPath(xpath, true);
        return jaxbNodes;
    }

    /*------------------------------------Word 段落相关---------------------------------------------------  */

    /**
     * @Description: 只删除单独的段落，不包括表格内或其他内的段落
     */
    public static boolean removeParaByIndex(WordprocessingMLPackage mlPackage, int index) {
        boolean flag = false;
        if (index < 0) {
            return flag;
        }
        List<Object> objList = mlPackage.getMainDocumentPart().getContent();
        if (objList == null) {
            return flag;
        }
        int k = -1;
        for (int i = 0, len = objList.size(); i < len; i++) {
            if (objList.get(i) instanceof P) {
                k++;
                if (k == index) {
                    mlPackage.getMainDocumentPart().getContent().remove(i);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    /**
     * @Description: 设置段落水平对齐方式
     */
    public static void setParaJcAlign(P paragraph, JcEnumeration hAlign) {
        if (hAlign != null) {
            PPr pprop = paragraph.getPPr();
            if (pprop == null) {
                pprop = new PPr();
                paragraph.setPPr(pprop);
            }
            Jc align = new Jc();
            align.setVal(hAlign);
            pprop.setJc(align);
        }
    }

    /**
     * @Description: 设置段落内容
     */
    public static void setParaRcontent(P p, RPr runProperties, String content) {
        R run = null;
        List<Object> rList = p.getContent();
        if (rList != null && rList.size() > 0) {
            for (int i = 0, len = rList.size(); i < len; i++) {
                // 清除内容(所有的r
                p.getContent().remove(0);
            }
        }
        run = new R();
        p.getContent().add(run);
        if (content != null) {
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(runProperties);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                // 换行
                run.getContent().add(br);
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(runProperties);
                run.getContent().add(text);
            }
        }
    }

    /**
     * @Description: 添加段落内容
     */
    public static void appendParaRcontent(P p, RPr runProperties, String content) {
        if (content != null) {
            R run = new R();
            p.getContent().add(run);
            String[] contentArr = content.split("\n");
            Text text = new Text();
            text.setSpace("preserve");
            text.setValue(contentArr[0]);
            run.setRPr(runProperties);
            run.getContent().add(text);

            for (int i = 1, len = contentArr.length; i < len; i++) {
                Br br = new Br();
                // 换行
                run.getContent().add(br);
                text = new Text();
                text.setSpace("preserve");
                text.setValue(contentArr[i]);
                run.setRPr(runProperties);
                run.getContent().add(text);
            }
        }
    }

    /**
     * @Description: 添加图片到段落
     */
    public static void addImageToPara(WordprocessingMLPackage mlPackage, ObjectFactory factory, P paragraph,
                                      String filePath, String content, RPr rpr, String altText, int id1, int id2) throws Exception {
        R run = factory.createR();
        if (content != null) {
            Text text = factory.createText();
            text.setValue(content);
            text.setSpace("preserve");
            run.setRPr(rpr);
            run.getContent().add(text);
        }

        InputStream is = new FileInputStream(filePath);
        byte[] bytes = IOUtils.toByteArray(is);
        BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(mlPackage, bytes);
        Inline inline = imagePart.createImageInline(filePath, altText, id1, id2, false);
        Drawing drawing = factory.createDrawing();
        drawing.getAnchorOrInline().add(inline);
        run.getContent().add(drawing);
        paragraph.getContent().add(run);
    }

    /**
     * @Description: 段落添加Br 页面Break(分页符)
     */
    public static void addPageBreak(P para, STBrType type) {
        Br breakObj = new Br();
        breakObj.setType(type);
        para.getContent().add(breakObj);
    }

    /**
     * @Description: 设置段落是否禁止行号(禁止用于当前行号)
     */
    public static void setParagraphSuppressLineNum(P p) {
        PPr ppr = getPpr(p);
        BooleanDefaultTrue line = ppr.getSuppressLineNumbers();
        if (line == null) {
            line = new BooleanDefaultTrue();
        }
        line.setVal(true);
        ppr.setSuppressLineNumbers(line);
    }

    /**
     * @Description: 设置段落底纹(对整段文字起作用)
     */
    public static void setParagraphShdStyle(P p, STShd shdType, String shdColor) {
        PPr ppr = getPpr(p);
        CTShd ctShd = ppr.getShd();
        if (ctShd == null) {
            ctShd = new CTShd();
        }
        if (StringUtils.isNotBlank(shdColor)) {
            ctShd.setColor(shdColor);
        }
        if (shdType != null) {
            ctShd.setVal(shdType);
        }
        ppr.setShd(ctShd);
    }

    /**
     * @param isSpace           是否设置段前段后值
     * @param before            段前磅数
     * @param after             段后磅数
     * @param beforeLines       段前行数
     * @param afterLines        段后行数
     * @param isLine            是否设置行距
     * @param lineValue         行距值
     * @param sTLineSpacingRule 自动auto 固定exact 最小 atLeast 1磅=20 1行=100 单倍行距=240
     */
    public static void setParagraphSpacing(P p, boolean isSpace, String before, String after, String beforeLines,
                                           String afterLines, boolean isLine, String lineValue, STLineSpacingRule rule) {
        PPr pPr = getPpr(p);
        Spacing spacing = pPr.getSpacing();
        if (spacing == null) {
            spacing = new Spacing();
            pPr.setSpacing(spacing);
        }
        if (isSpace) {
            if (StringUtils.isNotBlank(before)) {
                // 段前磅数
                spacing.setBefore(new BigInteger(before));
            }
            if (StringUtils.isNotBlank(after)) {
                // 段后磅数
                spacing.setAfter(new BigInteger(after));
            }
            if (StringUtils.isNotBlank(beforeLines)) {
                // 段前行数
                spacing.setBeforeLines(new BigInteger(beforeLines));
            }
            if (StringUtils.isNotBlank(afterLines)) {
                // 段后行数
                spacing.setAfterLines(new BigInteger(afterLines));
            }
        }
        if (isLine) {
            if (StringUtils.isNotBlank(lineValue)) {
                spacing.setLine(new BigInteger(lineValue));
            }
            if (rule != null) {
                spacing.setLineRule(rule);
            }
        }
    }

    /**
     * @Description: 设置段落缩进信息 1厘米≈567
     */
    public static void setParagraphIndInfo(P p, String firstLine, String firstLineChar, String hanging,
                                           String hangingChar, String right, String rigthChar, String left, String leftChar) {
        PPr ppr = getPpr(p);
        Ind ind = ppr.getInd();
        if (ind == null) {
            ind = new Ind();
            ppr.setInd(ind);
        }
        if (StringUtils.isNotBlank(firstLine)) {
            ind.setFirstLine(new BigInteger(firstLine));
        }
        if (StringUtils.isNotBlank(firstLineChar)) {
            ind.setFirstLineChars(new BigInteger(firstLineChar));
        }
        if (StringUtils.isNotBlank(hanging)) {
            ind.setHanging(new BigInteger(hanging));
        }
        if (StringUtils.isNotBlank(hangingChar)) {
            ind.setHangingChars(new BigInteger(hangingChar));
        }
        if (StringUtils.isNotBlank(left)) {
            ind.setLeft(new BigInteger(left));
        }
        if (StringUtils.isNotBlank(leftChar)) {
            ind.setLeftChars(new BigInteger(leftChar));
        }
        if (StringUtils.isNotBlank(right)) {
            ind.setRight(new BigInteger(right));
        }
        if (StringUtils.isNotBlank(rigthChar)) {
            ind.setRightChars(new BigInteger(rigthChar));
        }
    }

    public static PPr getPpr(P p) {
        PPr ppr = p.getPPr();
        if (ppr == null) {
            ppr = new PPr();
            p.setPPr(ppr);
        }
        return ppr;
    }

    public static ParaRPr getParaRpr(PPr ppr) {
        ParaRPr parRpr = ppr.getRPr();
        if (parRpr == null) {
            parRpr = new ParaRPr();
            ppr.setRPr(parRpr);
        }
        return parRpr;

    }

    public static void setParaVanish(PPr ppr, boolean isVanish) {
        ParaRPr parRpr = getParaRpr(ppr);
        BooleanDefaultTrue vanish = parRpr.getVanish();
        if (vanish != null) {
            vanish.setVal(isVanish);
        } else {
            vanish = new BooleanDefaultTrue();
            parRpr.setVanish(vanish);
            vanish.setVal(isVanish);
        }
    }

    /**
     * @Description: 设置段落边框样式
     */
    public static void setParagraghBorders(P p, CTBorder topBorder, CTBorder bottomBorder, CTBorder leftBorder,
                                           CTBorder rightBorder) {
        PPr ppr = getPpr(p);
        PBdr pBdr = new PBdr();
        if (topBorder != null) {
            pBdr.setTop(topBorder);
        }
        if (bottomBorder != null) {
            pBdr.setBottom(bottomBorder);
        }
        if (leftBorder != null) {
            pBdr.setLeft(leftBorder);
        }
        if (rightBorder != null) {
            pBdr.setRight(rightBorder);
        }
        ppr.setPBdr(pBdr);
    }

    /**
     * @Description: 设置字体信息
     */
    public static void setFontStyle(RPr runProperties, String cnFontFamily, String enFontFamily, String fontSize,
                                    String color) {
        setFontFamily(runProperties, cnFontFamily, enFontFamily);
        setFontSize(runProperties, fontSize);
        setFontColor(runProperties, color);
    }

    /**
     * @Description: 设置字体大小
     */
    public static void setFontSize(RPr runProperties, String fontSize) {
        if (StringUtils.isNotBlank(fontSize)) {
            HpsMeasure size = new HpsMeasure();
            size.setVal(new BigInteger(fontSize));
            runProperties.setSz(size);
            runProperties.setSzCs(size);
        }
    }

    /**
     * @Description: 设置字体
     */
    public static void setFontFamily(RPr runProperties, String cnFontFamily, String enFontFamily) {
        if (StringUtils.isNotBlank(cnFontFamily) || StringUtils.isNotBlank(enFontFamily)) {
            RFonts rf = runProperties.getRFonts();
            if (rf == null) {
                rf = new RFonts();
                runProperties.setRFonts(rf);
            }
            if (cnFontFamily != null) {
                rf.setEastAsia(cnFontFamily);
            }
            if (enFontFamily != null) {
                rf.setAscii(enFontFamily);
            }
        }
    }

    /**
     * @Description: 设置字体颜色
     */
    public static void setFontColor(RPr runProperties, String color) {
        if (color != null) {
            Color c = new Color();
            c.setVal(color);
            runProperties.setColor(c);
        }
    }

    /**
     * @Description: 设置字符边框
     */
    public static void addRrrBorderStyle(RPr runProperties, String size, STBorder bordType, String space,
                                         String color) {
        CTBorder value = new CTBorder();
        if (StringUtils.isNotBlank(color)) {
            value.setColor(color);
        }
        if (StringUtils.isNotBlank(size)) {
            value.setSz(new BigInteger(size));
        }
        if (StringUtils.isNotBlank(space)) {
            value.setSpace(new BigInteger(space));
        }
        if (bordType != null) {
            value.setVal(bordType);
        }
        runProperties.setBdr(value);
    }

    /**
     * @Description:着重号
     */
    public static void addRrrEmStyle(RPr runProperties, STEm emType) {
        if (emType != null) {
            CTEm em = new CTEm();
            em.setVal(emType);
            runProperties.setEm(em);
        }
    }

    /**
     * @Description: 空心
     */
    public static void addRrrOutlineStyle(RPr runProperties) {
        BooleanDefaultTrue outline = new BooleanDefaultTrue();
        outline.setVal(true);
        runProperties.setOutline(outline);
    }

    /**
     * @Description: 设置上标下标
     */
    public static void addRrrcaleStyle(RPr runProperties, STVerticalAlignRun vAlign) {
        if (vAlign != null) {
            CTVerticalAlignRun value = new CTVerticalAlignRun();
            value.setVal(vAlign);
            runProperties.setVertAlign(value);
        }
    }

    /**
     * @Description: 设置字符间距缩进
     */
    public static void addRrrScaleStyle(RPr runProperties, int indent) {
        CTTextScale value = new CTTextScale();
        value.setVal(indent);
        runProperties.setW(value);
    }

    /**
     * @Description: 设置字符间距信息
     */
    public static void addRrrtSpacingStyle(RPr runProperties, int spacing) {
        CTSignedTwipsMeasure value = new CTSignedTwipsMeasure();
        value.setVal(BigInteger.valueOf(spacing));
        runProperties.setSpacing(value);
    }

    /**
     * @Description: 设置文本位置
     */
    public static void addRrrtPositionStyle(RPr runProperties, int position) {
        CTSignedHpsMeasure ctPosition = new CTSignedHpsMeasure();
        ctPosition.setVal(BigInteger.valueOf(position));
        runProperties.setPosition(ctPosition);
    }

    /**
     * @Description: 阴文
     */
    public static void addRrrImprintStyle(RPr runProperties) {
        BooleanDefaultTrue imprint = new BooleanDefaultTrue();
        imprint.setVal(true);
        runProperties.setImprint(imprint);
    }

    /**
     * @Description: 阳文
     */
    public static void addRrrEmbossStyle(RPr runProperties) {
        BooleanDefaultTrue emboss = new BooleanDefaultTrue();
        emboss.setVal(true);
        runProperties.setEmboss(emboss);
    }

    /**
     * @Description: 设置隐藏
     */
    public static void setRprVanishStyle(RPr runProperties, boolean isVanish) {
        BooleanDefaultTrue vanish = runProperties.getVanish();
        if (vanish != null) {
            vanish.setVal(isVanish);
        } else {
            vanish = new BooleanDefaultTrue();
            vanish.setVal(isVanish);
            runProperties.setVanish(vanish);
        }
    }

    /**
     * @Description: 设置阴影
     */
    public static void addRrrShadowStyle(RPr runProperties) {
        BooleanDefaultTrue shadow = new BooleanDefaultTrue();
        shadow.setVal(true);
        runProperties.setShadow(shadow);
    }

    /**
     * @Description: 设置底纹
     */
    public static void addRrrShdStyle(RPr runProperties, STShd shdtype) {
        if (shdtype != null) {
            CTShd shd = new CTShd();
            shd.setVal(shdtype);
            runProperties.setShd(shd);
        }
    }

    /**
     * @Description: 设置突出显示文本
     */
    public static void addRrrHightLightStyle(RPr runProperties, String hightlight) {
        if (StringUtils.isNotBlank(hightlight)) {
            Highlight highlight = new Highlight();
            highlight.setVal(hightlight);
            runProperties.setHighlight(highlight);
        }
    }

    /**
     * @Description: 设置删除线样式
     */
    public static void addRrrStrikeStyle(RPr runProperties, boolean isStrike, boolean isds) {
        // 删除线
        if (isStrike) {
            BooleanDefaultTrue strike = new BooleanDefaultTrue();
            strike.setVal(true);
            runProperties.setStrike(strike);
        }
        // 双删除线
        if (isds) {
            BooleanDefaultTrue dStrike = new BooleanDefaultTrue();
            dStrike.setVal(true);
            runProperties.setDstrike(dStrike);
        }
    }

    /**
     * @Description: 加粗
     */
    public static void addRrrBoldStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setB(b);
    }

    /**
     * @Description: 倾斜
     */
    public static void addRrrItalicStyle(RPr runProperties) {
        BooleanDefaultTrue b = new BooleanDefaultTrue();
        b.setVal(true);
        runProperties.setI(b);
    }

    /**
     * @Description: 添加下划线
     */
    public static void addRprUnderlineStyle(RPr runProperties, UnderlineEnumeration enumType) {
        U val = new U();
        val.setVal(enumType);
        runProperties.setU(val);
    }

    /*------------------------------------Word 相关---------------------------------------------------  */

    /**
     * @Description: 设置分节符 nextPage:下一页 continuous:连续 evenPage:偶数页 oddPage:奇数页
     */
    public static void setDocSectionBreak(WordprocessingMLPackage wordPackage, String sectValType) {
        if (StringUtils.isNotBlank(sectValType)) {
            SectPr sectPr = getDocSectPr(wordPackage);
            Type sectType = sectPr.getType();
            if (sectType == null) {
                sectType = new Type();
                sectPr.setType(sectType);
            }
            sectType.setVal(sectValType);
        }
    }

    /**
     * @Description: 设置页面背景色
     */
    public static void setDocumentBackGround(WordprocessingMLPackage wordPackage, ObjectFactory factory, String color)
            throws Exception {
        MainDocumentPart mdp = wordPackage.getMainDocumentPart();
        CTBackground bkground = mdp.getContents().getBackground();
        if (StringUtils.isNotBlank(color)) {
            if (bkground == null) {
                bkground = factory.createCTBackground();
                bkground.setColor(color);
            }
            mdp.getContents().setBackground(bkground);
        }
    }

    /**
     * @Description: 设置页面边框
     */
    public static void setDocumentBorders(WordprocessingMLPackage wordPackage, ObjectFactory factory, CTBorder top,
                                          CTBorder right, CTBorder bottom, CTBorder left) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgBorders pgBorders = sectPr.getPgBorders();
        if (pgBorders == null) {
            pgBorders = factory.createSectPrPgBorders();
            sectPr.setPgBorders(pgBorders);
        }
        if (top != null) {
            pgBorders.setTop(top);
        }
        if (right != null) {
            pgBorders.setRight(right);
        }
        if (bottom != null) {
            pgBorders.setBottom(bottom);
        }
        if (left != null) {
            pgBorders.setLeft(left);
        }
    }

    /**
     * @Description: 设置页面大小及纸张方向 landscape横向
     */
    public static void setDocumentSize(WordprocessingMLPackage wordPackage, ObjectFactory factory, String width,
                                       String height, STPageOrientation stValue) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgSz pgSz = sectPr.getPgSz();
        if (pgSz == null) {
            pgSz = factory.createSectPrPgSz();
            sectPr.setPgSz(pgSz);
        }
        if (StringUtils.isNotBlank(width)) {
            pgSz.setW(new BigInteger(width));
        }
        if (StringUtils.isNotBlank(height)) {
            pgSz.setH(new BigInteger(height));
        }
        if (stValue != null) {
            pgSz.setOrient(stValue);
        }
    }

    public static SectPr getDocSectPr(WordprocessingMLPackage wordPackage) {
        SectPr sectPr = wordPackage.getDocumentModel().getSections().get(0).getSectPr();
        return sectPr;
    }

    /**
     * @Description：设置页边距
     */
    public static void setDocMarginSpace(WordprocessingMLPackage wordPackage, ObjectFactory factory, String top,
                                         String left, String bottom, String right) {
        SectPr sectPr = getDocSectPr(wordPackage);
        PgMar pg = sectPr.getPgMar();
        if (pg == null) {
            pg = factory.createSectPrPgMar();
            sectPr.setPgMar(pg);
        }
        if (StringUtils.isNotBlank(top)) {
            pg.setTop(new BigInteger(top));
        }
        if (StringUtils.isNotBlank(bottom)) {
            pg.setBottom(new BigInteger(bottom));
        }
        if (StringUtils.isNotBlank(left)) {
            pg.setLeft(new BigInteger(left));
        }
        if (StringUtils.isNotBlank(right)) {
            pg.setRight(new BigInteger(right));
        }
    }

    /**
     * @param distance    :距正文距离 1厘米=567
     * @param start       :起始编号(0开始)
     * @param countBy     :行号间隔
     * @param restartType :STLineNumberRestart.CONTINUOUS(continuous连续编号)<br/>
     *                    STLineNumberRestart.NEW_PAGE(每页重新编号)<br/>
     *                    STLineNumberRestart.NEW_SECTION(每节重新编号)
     * @Description: 设置行号
     */
    public static void setDocInNumType(WordprocessingMLPackage wordPackage, String countBy, String distance,
                                       String start, STLineNumberRestart restartType) {
        SectPr sectPr = getDocSectPr(wordPackage);
        CTLineNumber lnNumType = sectPr.getLnNumType();
        if (lnNumType == null) {
            lnNumType = new CTLineNumber();
            sectPr.setLnNumType(lnNumType);
        }
        if (StringUtils.isNotBlank(countBy)) {
            lnNumType.setCountBy(new BigInteger(countBy));
        }
        if (StringUtils.isNotBlank(distance)) {
            lnNumType.setDistance(new BigInteger(distance));
        }
        if (StringUtils.isNotBlank(start)) {
            lnNumType.setStart(new BigInteger(start));
        }
        if (restartType != null) {
            lnNumType.setRestart(restartType);
        }
    }

    /**
     * @Description：设置文字方向 tbRl 垂直
     */
    public static void setDocTextDirection(WordprocessingMLPackage wordPackage, String textDirection) {
        if (StringUtils.isNotBlank(textDirection)) {
            SectPr sectPr = getDocSectPr(wordPackage);
            TextDirection textDir = sectPr.getTextDirection();
            if (textDir == null) {
                textDir = new TextDirection();
                sectPr.setTextDirection(textDir);
            }
            textDir.setVal(textDirection);
        }
    }

    /**
     * @Description：设置word 垂直对齐方式(Word默认方式都是 " 顶端对齐 ")
     */
    public static void setDocValign(WordprocessingMLPackage wordPackage, STVerticalJc valignType) {
        if (valignType != null) {
            SectPr sectPr = getDocSectPr(wordPackage);
            CTVerticalJc valign = sectPr.getVAlign();
            if (valign == null) {
                valign = new CTVerticalJc();
                sectPr.setVAlign(valign);
            }
            valign.setVal(valignType);
        }
    }

    /**
     * @Description：获取文档的可用宽度
     */
    public static int getWritableWidth(WordprocessingMLPackage wordPackage) throws Exception {
        return wordPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
    }

    /**
     * Factory for creating WML Object.
     */
    private static final ObjectFactory FACTORY = Context.getWmlObjectFactory();


    /**
     * 把docx转成html
     *
     * @param docxFilePath
     * @param htmlPath
     * @throws Exception
     */
    public static void convertDocxToHtml(String docxFilePath, String htmlPath) throws Exception {

        WordprocessingMLPackage load = Docx4J.load(new File(docxFilePath));

        HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
        String imageFilePath = htmlPath.substring(0, htmlPath.lastIndexOf("\\") + 1) + "/images";
        htmlSettings.setImageDirPath(imageFilePath);
        htmlSettings.setImageTargetUri("images");
        htmlSettings.setWmlPackage(load);

        htmlSettings.setStyleElementHandler((opcPackage, document, styleDefinition) -> {
            Element ret = null;
            if (styleDefinition != null && styleDefinition.length() > 0) {
                ret = document.createElement("style");
                ret.appendChild(document.createComment(styleDefinition));
            }

            return ret;
        });

        OutputStream os;

        os = new FileOutputStream(htmlPath);

        Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);

        Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        log.info("转换完成");
    }

    public static void docxToHtml(String fileUrl) throws Exception {
        String path = fileUrl.substring(0, fileUrl.indexOf("."));
        File file = new File(fileUrl);
        WordprocessingMLPackage load = Docx4J.load(file);
        HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
        String imageFilePath = path + "/images/";
        if (!new File(imageFilePath).exists()) {
            new File(imageFilePath).mkdirs();
        }
        htmlSettings.setImageDirPath(imageFilePath);
        htmlSettings.setImageTargetUri("images");
        htmlSettings.setWmlPackage(load);
        htmlSettings.setStyleElementHandler((opcPackage, document, styleDefinition) -> {
            Element ret = null;
            if (styleDefinition != null && styleDefinition.length() > 0) {
                ret = document.createElement("style");
                ret.appendChild(document.createComment(styleDefinition));
            }
            return ret;
        });
        OutputStream os = new FileOutputStream(path + "/" + file.getName().substring(0, file.getName().indexOf(".")) + ".html");
        Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
        Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
        log.info("转换完成");
    }

//    @SneakyThrows
//    public static void main(String[] args) {
//        String s = HttpUtil.get("https://blog.csdn.net/u012915733/article/details/79554434", 200);
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(s.getBytes());
//        FileOutputStream fileOutputStream = new FileOutputStream("f://test.docx");
//        html2Word(byteArrayInputStream, fileOutputStream);
//    }

    /**
     * 网页转文件
     * Convert HTML to DOCX(OPENXML).
     *
     * @param inputStream the source, a HTML file   网页流
     * @param docx        the target, a DOCX file  文件流
     */
    public static void html2Word(InputStream inputStream, OutputStream docx) {
        try {
            // Creates a WordprocessingMLPackage, using default page size and orientation,
            // default to A4 portrait.
            WordprocessingMLPackage pack = WordprocessingMLPackage.createPackage();
            // Convert XHTML + CSS to WordML content.
            // XHTML must be well formed XML.
            XHTMLImporterImpl importer = new XHTMLImporterImpl(pack);
            // Convert the well formed XHTML contained in file to a list of WML objects.
            List<Object> list = importer.convert(inputStream, null);
            MainDocumentPart mainPart = pack.getMainDocumentPart();
            // Add all WML objects to MainDocumentPart.
            mainPart.getContent().addAll(list);
            // create footer
            Ftr footer = createFooterWithPageNumber();
            FooterPart footerPart = new FooterPart();
            footerPart.setPackage(pack);
            footerPart.setJaxbElement(footer);
            Relationship footerRelation = mainPart.addTargetPart(footerPart);
            FooterReference footerRef = FACTORY.createFooterReference();
            footerRef.setId(footerRelation.getId());
            footerRef.setType(HdrFtrRef.DEFAULT);
            SectPr sectPr = pack.getDocumentModel().getSections().get(0).getSectPr();
            sectPr.getEGHdrFtrReferences().add(footerRef);
            // create header
            Hdr header = createHeader("重庆****科技有限公司");
            HeaderPart headerPart = new HeaderPart();
            headerPart.setPackage(pack);
            headerPart.setJaxbElement(header);
            Relationship headerRelation = mainPart.addTargetPart(headerPart);
            HeaderReference headerRef = FACTORY.createHeaderReference();
            headerRef.setId(headerRelation.getId());
            headerRef.setType(HdrFtrRef.DEFAULT);
            sectPr.getEGHdrFtrReferences().add(headerRef);
            // set compatibilityMode to 15
            // to avoid Word 365/2016 saying "Compatibility Mode"
            DocumentSettingsPart settingsPart = mainPart.getDocumentSettingsPart(true);
            CTCompat compat = FACTORY.createCTCompat();
            compat.setCompatSetting("compatibilityMode", "http://schemas.microsoft.com/office/word", "15");
            settingsPart.getContents().setCompat(compat);
            // save to a file
            pack.save(docx);
        } catch (Docx4JException e) {
            throw new BusinessException("covert html to docx error.", e);
        }
    }

    /**
     * create a header
     *
     * @param content
     * @return
     */
    private static Hdr createHeader(String content) {
        Hdr header = FACTORY.createHdr();
        P paragraph = FACTORY.createP();
        PPr ppr = FACTORY.createPPr();
        Jc jc = FACTORY.createJc();
        jc.setVal(JcEnumeration.CENTER);
        ppr.setJc(jc);
        paragraph.setPPr(ppr);
        R run = FACTORY.createR();
        Text text = new Text();
        text.setValue(content);
        run.getContent().add(text);
        paragraph.getContent().add(run);
        header.getContent().add(paragraph);
        return header;
    }

    /**
     * create a header with page number
     *
     * @return
     */
    private static Ftr createFooterWithPageNumber() {
        Ftr ftr = FACTORY.createFtr();
        P paragraph = FACTORY.createP();
        PPr ppr = FACTORY.createPPr();
        Jc jc = FACTORY.createJc();
        jc.setVal(JcEnumeration.RIGHT);
        ppr.setJc(jc);
        paragraph.setPPr(ppr);
        addFieldBegin(paragraph);
        addPageNumberField(paragraph);
        addFieldEnd(paragraph);
        ftr.getContent().add(paragraph);
        return ftr;
    }

    private static void addPageNumberField(P paragraph) {
        Text txt = new Text();
        txt.setSpace("preserve");
        txt.setValue(" PAGE   \\* ArabicDash ");
        R run = FACTORY.createR();
        run.getContent().add(FACTORY.createRInstrText(txt));
        paragraph.getContent().add(run);
    }

    private static void addFieldBegin(P paragraph) {
        FldChar fldChar = FACTORY.createFldChar();
        fldChar.setFldCharType(STFldCharType.BEGIN);
        R run = FACTORY.createR();
        run.getContent().add(fldChar);
        paragraph.getContent().add(run);
    }

    private static void addFieldEnd(P paragraph) {
        FldChar fldChar = FACTORY.createFldChar();
        fldChar.setFldCharType(STFldCharType.END);
        R run = FACTORY.createR();
        run.getContent().add(fldChar);
        paragraph.getContent().add(run);
    }
}
