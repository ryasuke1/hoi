package khubanov.ochir;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.OutputStream;
import java.nio.file.Path;

public class PeopleJaxbWriter {

    public void write(JaxbModel.JaxbPeople people, OutputStream out, Path xsdPath) throws Exception {
        JAXBContext context = JAXBContext.newInstance(JaxbModel.JaxbPeople.class);
        Marshaller marshaller = context.createMarshaller();

        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        if (xsdPath != null) {
            // Invalid output will fail during marshalling instead of silently producing bad XML.
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(xsdPath.toFile());
            marshaller.setSchema(schema);
        }

        marshaller.marshal(people, out);
    }
}
