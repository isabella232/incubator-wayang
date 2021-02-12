package org.apache.wayang.api.python.executor;

import org.apache.wayang.api.python.function.PythonUdf;
import org.apache.wayang.core.api.exception.WayangException;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

public class ProcessFeeder<Input, Output> {

    private Socket socket;
    private PythonUdf<Input, Output> udf;
    private Iterable<Input> input;

    //TODO add to a config file
    int END_OF_DATA_SECTION = -1;
    int NULL = -5;

    public ProcessFeeder(
            Socket socket,
            PythonUdf<Input, Output> udf,
            Iterable<Input> input){

        if(input == null) throw new WayangException("Nothing to process with Python API");

        this.socket = socket;
        this.udf = udf;
        this.input = input;

    }

    public void send(){

        try{
            //TODO use config buffer size
            int BUFFER_SIZE = 8192;

            BufferedOutputStream stream = new BufferedOutputStream(socket.getOutputStream(), BUFFER_SIZE);
            DataOutputStream dataOut = new DataOutputStream(stream);

            this.writeIteratorToStream(input.iterator(), dataOut);
            dataOut.writeInt(END_OF_DATA_SECTION);
            dataOut.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeIteratorToStream(Iterator<Input> iter, DataOutputStream dataOut){

        for (Iterator<Input> it = iter; it.hasNext(); ) {
            Input elem = it.next();
            write(elem, dataOut);
        }
    }

    /*TODO Missing case PortableDataStream */
    public void write(Object obj, DataOutputStream dataOut){
        try {

            if(obj == null)
                dataOut.writeInt(this.NULL);

            /**
             * Byte Array cases
             */
            else if (obj instanceof Byte[] || obj instanceof byte[])
                writeBytes(obj, dataOut);

            /**
             * String case
             * */
            else if (obj instanceof String)
                writeUTF((String) obj, dataOut);

            /**
             * Key, Value case
             * */
            else if (obj instanceof Map.Entry)
                writeKeyValue((Map.Entry) obj, dataOut);

            else{
                throw new WayangException("Unexpected element type " + obj.getClass());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeBytes(Object obj, DataOutputStream dataOut){

        try{

            if (obj instanceof Byte[]) {

                int length = ((Byte[]) obj).length;

                byte[] bytes = new byte[length];
                int j=0;

                // Unboxing Byte values. (Byte[] to byte[])
                for(Byte b: ((Byte[]) obj))
                    bytes[j++] = b.byteValue();

                dataOut.writeInt(length);
                dataOut.write(bytes);

            } else if (obj instanceof byte[]) {

                dataOut.writeInt(((byte[]) obj).length);
                dataOut.write(((byte[]) obj));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeUTF(String str, DataOutputStream dataOut){

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);

        try {

            dataOut.writeInt(bytes.length);
            dataOut.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeKeyValue(Map.Entry obj, DataOutputStream dataOut){

        write(obj.getKey(), dataOut);
        write(obj.getValue(), dataOut);
    }

}
