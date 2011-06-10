/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ion.integration.eoi;

import ion.core.utils.GPBWrapper;
import ion.core.utils.IonTime;
import ion.core.utils.IonUtils;
import ion.core.utils.ProtoUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.ooici.core.container.Container;
import net.ooici.core.message.IonMessage;
import net.ooici.integration.ais.AisRequestResponse;
import net.ooici.integration.ais.manageDataResource.ManageDataResource;
import net.ooici.services.sa.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author cmueller
 */
public class DataResourceBuilder {

    private static final Logger log = LoggerFactory.getLogger(DataResourceBuilder.class);

    public static Container.Structure getDataResourceCreateRequestStructure(String filePath, final StringBuilder sb) throws FileNotFoundException, IOException, Exception {
        File infile = new File(filePath);
        if (!infile.exists()) {
            throw new FileNotFoundException("Missing file containing the context object in json form");
        }

        Container.Structure struct = null;
        GPBWrapper<DataSource.ThreddsAuthentication> tdsWrap = null;
        GPBWrapper<DataSource.SearchPattern> srchWrap = null;
        List<GPBWrapper<DataSource.SubRange>> subRngList = new ArrayList<GPBWrapper<DataSource.SubRange>>();
        ManageDataResource.DataResourceCreateRequest.Builder dscrBldr = null;

        String fileContent = readFile(infile.getCanonicalPath());
        Pattern p = Pattern.compile("(?m)#\\s*[a-zA-Z]+?:([0-9]+)\\s*(\\{[^{}]+?\\})");
        Matcher m = p.matcher(fileContent);
        while (m.find()) {
            int resId = Integer.valueOf(fileContent.substring(m.start(1), m.end(1)));
            String json = fileContent.substring(m.start(2), m.end(2));
            switch (resId) {
                case 9211://DataResourceCreateRequest
                    dscrBldr = (ManageDataResource.DataResourceCreateRequest.Builder) IonUtils.convertJsonToGPBBuilder(json, resId);
                    break;
                case 4504://ThreddsAuthentication
                    tdsWrap = GPBWrapper.Factory((DataSource.ThreddsAuthentication) IonUtils.convertJsonToGPB(json, resId));
                    break;
                case 4505://SearchPattern
                    srchWrap = GPBWrapper.Factory((DataSource.SearchPattern) IonUtils.convertJsonToGPB(json, resId));
                    break;
                case 4506://SubRange - can be repeated
                    subRngList.add(GPBWrapper.Factory((DataSource.SubRange) IonUtils.convertJsonToGPB(json, resId)));
                    break;
            }
        }
        if (dscrBldr != null) {
            Container.Structure.Builder sbldr = Container.Structure.newBuilder();
            if (tdsWrap != null) {
                dscrBldr.setAuthentication(tdsWrap.getCASRef());
                ProtoUtils.addStructureElementToStructureBuilder(sbldr, tdsWrap.getStructureElement());
            }
            if (srchWrap != null) {
                dscrBldr.setSearchPattern(srchWrap.getCASRef());
                ProtoUtils.addStructureElementToStructureBuilder(sbldr, srchWrap.getStructureElement());
            }
            if (!subRngList.isEmpty()) {
                for(GPBWrapper<DataSource.SubRange> rng : subRngList) {
                    dscrBldr.addSubRanges(rng.getObjectValue());
                }
            }

            /* Set the update_start_datetime_millis field to now */
            dscrBldr.setUpdateStartDatetimeMillis(IonTime.now().getMillis());

            GPBWrapper<ManageDataResource.DataResourceCreateRequest> dscrWrap = GPBWrapper.Factory(dscrBldr.build());
            log.debug(dscrWrap.toString());
            String nl = System.getProperty("line.separator");
            sb.append("Resource Registered:").append(nl).append("*************************").append(nl);
            sb.append(dscrWrap.getObjectValue()).append("*************************").append(nl);
            ProtoUtils.addStructureElementToStructureBuilder(sbldr, dscrWrap.getStructureElement());

            GPBWrapper<AisRequestResponse.ApplicationIntegrationServiceRequestMsg> aisReqMsgWrap = GPBWrapper.Factory(AisRequestResponse.ApplicationIntegrationServiceRequestMsg.newBuilder().setMessageParametersReference(dscrWrap.getCASRef()).build());
            log.debug(aisReqMsgWrap.toString());
            ProtoUtils.addStructureElementToStructureBuilder(sbldr, aisReqMsgWrap.getStructureElement());

            IonMessage.IonMsg ionMsg = IonMessage.IonMsg.newBuilder().setIdentity(UUID.randomUUID().toString()).setMessageObject(aisReqMsgWrap.getCASRef()).build();
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
