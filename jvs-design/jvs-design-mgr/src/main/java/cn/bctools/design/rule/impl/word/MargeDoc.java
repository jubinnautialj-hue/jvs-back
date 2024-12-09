package cn.bctools.design.rule.impl.word;

import cn.bctools.common.utils.ObjectNull;
import cn.bctools.word.utils.Tool;
import cn.bctools.word.utils.Tool;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.SneakyThrows;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.Graphic;
import org.docx4j.dml.GraphicData;
import org.docx4j.dml.picture.Pic;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.contenttype.ContentTypeManager;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.*;

import javax.xml.bind.JAXBElement;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * @author jvs
 * 合并 doc 格式
 */
public class MargeDoc {

    /**
     * Merge docx.
     *
     * @param list         the list
     * @param outputStream the output stream
     * @param newPage      the new page
     */
    public static void mergeDocx(List<String> list, OutputStream outputStream, Boolean newPage) {
        List<InputStream> inList = new ArrayList<InputStream>();
        for (int i = 0; i < list.size(); i++) {
            inList.add(new ByteArrayInputStream(HttpUtil.downloadBytes(list.get(i))));
        }
        try {
            InputStream inputStream = mergeDocx(inList, newPage);
            saveTemplate(inputStream, outputStream);
        } catch (Docx4JException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Merge docx input stream.
     *
     * @param streams the streams
     * @param newPage the new page
     * @return the input stream
     * @throws Docx4JException the docx 4 j exception
     * @throws IOException     the io exception
     */
    public static InputStream mergeDocx(final List<InputStream> streams, Boolean newPage) throws Docx4JException, IOException {
        WordprocessingMLPackage target = null;
        final File generated = File.createTempFile("generated", ".docx");
        int chunkId = 0;
        Iterator<InputStream> it = streams.iterator();
        while (it.hasNext()) {
            InputStream is = it.next();
            if (is != null) {
                if (target == null) {
                    OutputStream os = Files.newOutputStream(generated.toPath());
                    os.write(IOUtils.toByteArray(is));
                    os.close();
                    target = WordprocessingMLPackage.load(generated);
                    updateImageReferences(target);
                } else {
                    insertDocx(target, IOUtils.toByteArray(is), chunkId++, newPage);
                }
            }
        }

        if (target != null) {
            target.save(generated);
            return Files.newInputStream(generated.toPath());
        } else {
            return null;
        }
    }

    /**
     * 插入文档
     *
     * @param mainDoc
     * @param bytes
     * @param chunkId
     * @param newPage
     */
    private static void insertDocx(WordprocessingMLPackage mainDoc, byte[] bytes, int chunkId, Boolean newPage) {
        try {
            MainDocumentPart main = mainDoc.getMainDocumentPart();
            // 换页
            if (newPage) {
                SectPr sectPr = main.getContents().getBody().getSectPr();
                PPr pPr = new PPr();
                pPr.setSectPr(sectPr);
                P p = new P();
                p.setPPr(pPr);
                main.getContent().add(p);
            }
            // 插入新页
            WordprocessingMLPackage target = WordprocessingMLPackage.load(new ByteArrayInputStream(bytes));

            // 修改图片引用
            updateImageReferences(target);

            main.getContent().addAll(target.getMainDocumentPart().getContent());
            main.getRelationshipsPart().getRelationships().getRelationship().addAll(target.getMainDocumentPart().getRelationshipsPart().getRelationships().getRelationship());
            target.getParts().getParts().values().stream().filter(part -> part instanceof BinaryPartAbstractImage).forEach(part -> {
                mainDoc.getParts().put(part);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save template.
     *
     * @param fis          the fis
     * @param outputStream the output stream
     */
    /**
     * 修改图片引用
     * <p>
     *     若合并的多个word中存在图片，则合并后可能出现显示图片不正确的情况。
     *     因为每个word在合并前，其本身的图片引用id可能是相同的，图片路径也相同。导致合并后，显示的图片不正确
     *
     * @param doc
     */
    public static void updateImageReferences(WordprocessingMLPackage doc) {
        // 找到所有图片，修改图片引用，确保合并后图片引用不相同
        for (Object o : doc.getMainDocumentPart().getContent()) {
            if (o instanceof JAXBElement) {
                JAXBElement jaxbElement = (JAXBElement) o;
                if (jaxbElement.getValue() instanceof Tbl) {
                    Tbl table = (Tbl) jaxbElement.getValue();
                    // 修改表格中的图片引用
                    List<Object> trList = table.getContent();
                    for (Object objectTr : trList) {
                        List<Object> pList = Tool.getAllElementFromObject(objectTr, P.class);
                        if (ObjectNull.isNull(pList)) {
                            continue;
                        }
                        pList.forEach(p -> updatePImageReferences(doc, (P)p));
                        System.err.println(1);
                    }
                }
            }
            if (o instanceof P) {
                P p = (P) o;
                updatePImageReferences(doc, p);
            }
        }
    }

    /**
     * 在P元素中找到图片，并修改图片引用
     *
     * @param doc
     * @param p
     */
    @SneakyThrows
    private static void updatePImageReferences(WordprocessingMLPackage doc, P p) {
        List<Object> rList = Tool.getAllElementFromObject(p, R.class);
        for (Object robj : rList) {
            if (robj instanceof R) {
                R r = (R) robj;
                List<Object> drawList = Tool.getAllElementFromObject(r, Drawing.class);
                for (Object dobj : drawList) {
                    if (dobj instanceof Drawing) {
                        Drawing d = (Drawing) dobj;
                        Inline inline = (Inline) d.getAnchorOrInline().get(0);
                        Graphic g = inline.getGraphic();
                        GraphicData graphicdata = g.getGraphicData();
                        Pic pic = (Pic) XmlUtils.unwrap(graphicdata.getAny().get(0));
                        // 旧relId
                        String relId = pic.getBlipFill().getBlip().getEmbed();
                        // 新relId
                        String newRelId = "rId" + IdWorker.getIdStr();
                        // 替换新的relId
                        pic.getBlipFill().getBlip().setEmbed(newRelId);

                        // 还需要替换Relationship的引用，以及生成新的图片part
                        Optional<Relationship> optionalRelationship = doc.getMainDocumentPart().getRelationshipsPart()
                                .getRelationships()
                                .getRelationship()
                                .stream()
                                .filter(relation -> relation.getId().equals(relId))
                                .findFirst();
                        if (optionalRelationship.isPresent()) {
                            Relationship relationship = optionalRelationship.get();
                            String oldTarget = relationship.getTarget();
                            String newTarget = oldTarget.replace(relId, newRelId);
                            // 找到对应的target
                            Optional<Map.Entry<PartName, Part>> optionalPart = doc.getParts().getParts().entrySet().stream()
                                    .filter(key -> key.getKey().getName().contains(relId)).findFirst();
                            if (optionalPart.isPresent()) {
                                Map.Entry<PartName, Part> part = optionalPart.get();
                                PartName oldPartName = part.getKey();
                                doc.getParts().remove(oldPartName);
                                String newName = oldPartName.getName().replace(oldTarget, newTarget);
                                PartName newPartName = new PartName(newName);
                                // 生成新的图片part
                                BinaryPartAbstractImage imagePart = (BinaryPartAbstractImage) part.getValue();
                                BinaryPartAbstractImage newImagePart = createImagePart(doc, imagePart, newRelId);
                                newImagePart.setPartName(newPartName);
                                doc.getParts().put(newImagePart);
                            }
                            relationship.setId(newRelId);
                            relationship.setTarget(newTarget);
                        }

                    }
                }
            }
        }
    }

    /**
     * 根据已有图片part生成新的图片part，并修改图片地址
     *
     * @param doc
     * @param oldImagePart
     * @param proposedRelId 新的引用名（图片地址的组成部分）
     * @return
     * @throws Exception
     */
    public static BinaryPartAbstractImage createImagePart(WordprocessingMLPackage doc, BinaryPartAbstractImage oldImagePart, String proposedRelId) throws Exception {
        Part sourcePart = doc.getMainDocumentPart();
        ContentTypeManager ctm = doc.getContentTypeManager();
        String ext = oldImagePart.getPartName().getExtension();
        BinaryPartAbstractImage imagePart =
                (BinaryPartAbstractImage) ctm.newPartForContentType(
                        oldImagePart.getContentType(),
                        BinaryPartAbstractImage.createImageName(doc, doc.getMainDocumentPart(), proposedRelId, ext), null);
        imagePart.setBinaryData(oldImagePart.getBytes());
        imagePart.getRels().add(sourcePart.addTargetPart(imagePart, proposedRelId));
        return imagePart;

    }

    public static void saveTemplate(InputStream fis, OutputStream outputStream) {
        int bytesum = 0;
        int byteread;
        try {
            byte[] buffer = new byte[1444];
            while ((byteread = fis.read(buffer)) != -1) {
                // 字节数 文件大小
                bytesum += byteread;
                outputStream.write(buffer, 0, byteread);
            }
            fis.close();
            outputStream.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
