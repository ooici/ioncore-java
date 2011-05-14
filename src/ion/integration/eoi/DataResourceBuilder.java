/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.integration.eoi;

import ion.core.utils.GPBWrapper;
import ion.core.utils.IonUtils;
import ion.core.utils.ProtoUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author cmueller
 */
public class DataResourceBuilder {

    public static net.ooici.core.container.Container.Structure getDataResourceCreateRequestStructure(String filePath) throws FileNotFoundException, IOException, Exception {
        File infile = new File(filePath);
        if (!infile.exists()) {
            throw new FileNotFoundException("Missing file containing the context object in json form");
        }

        net.ooici.core.container.Container.Structure struct = null;
        GPBWrapper<net.ooici.services.sa.DataSource.ThreddsAuthentication> tdsWrap = null;
        GPBWrapper<net.ooici.services.sa.DataSource.SearchPattern> srchWrap = null;
        net.ooici.integration.ais.manageDataResource.ManageDataResource.DataResourceCreateRequest.Builder dscrBldr = null;

        String fileContent = readFile(infile.getCanonicalPath());
        Pattern p = Pattern.compile("(?m)#\\s*[a-zA-Z]+?:([0-9]+)\\s*(\\{[^{}]+?\\})");
        Matcher m = p.matcher(fileContent);
        while (m.find()) {
            int resId = Integer.valueOf(fileContent.substring(m.start(1), m.end(1)));
            String json = fileContent.substring(m.start(2), m.end(2));
            switch (resId) {
                case 9211://DataResourceCreateRequest
                    dscrBldr = (net.ooici.integration.ais.manageDataResource.ManageDataResource.DataResourceCreateRequest.Builder) IonUtils.convertJsonToGPBBuilder(json, resId);
                    break;
                case 4504://ThreddsAuthentication
                    tdsWrap = GPBWrapper.Factory((net.ooici.services.sa.DataSource.ThreddsAuthentication) IonUtils.convertJsonToGPB(json, resId));
                    break;
                case 4505://SearchPattern
                    srchWrap = GPBWrapper.Factory((net.ooici.services.sa.DataSource.SearchPattern) IonUtils.convertJsonToGPB(json, resId));
                    break;
            }
        }
        if (dscrBldr != null) {
            net.ooici.core.container.Container.Structure.Builder sbldr = net.ooici.core.container.Container.Structure.newBuilder();
            if (tdsWrap != null) {
                dscrBldr.setAuthentication(tdsWrap.getCASRef());
                ProtoUtils.addStructureElementToStructureBuilder(sbldr, tdsWrap.getStructureElement());
            }
            if (srchWrap != null) {
                dscrBldr.setSearchPattern(srchWrap.getCASRef());
                ProtoUtils.addStructureElementToStructureBuilder(sbldr, srchWrap.getStructureElement());
            }
            GPBWrapper<net.ooici.integration.ais.manageDataResource.ManageDataResource.DataResourceCreateRequest> dscrWrap = GPBWrapper.Factory(dscrBldr.build());
            ProtoUtils.addStructureElementToStructureBuilder(sbldr, dscrWrap.getStructureElement());

            net.ooici.core.message.IonMessage.IonMsg ionMsg = net.ooici.core.message.IonMessage.IonMsg.newBuilder().setIdentity(UUID.randomUUID().toString()).setMessageObject(dscrWrap.getCASRef()).build();
            ProtoUtils.addStructureElementToStructureBuilder(sbldr, GPBWrapper.Factory(ionMsg).getStructureElement(), true);

            /* Do something with the structure*/
            struct = sbldr.build();
        }

        return struct;
    }

    private static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            /* Instead of using default, pass in a decoder. */
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
