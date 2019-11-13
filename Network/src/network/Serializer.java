package network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public final class Serializer {
	/**
	 * Converts an Object to a byte array.
	 * 
	 * @param object, the Object to serialize. @return, the byte array that stores
	 *                the serialized object.
	 */
	public static byte[] serialize(Object object) {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(object);

			byte[] byteArray = bos.toByteArray();
			return byteArray;

		} catch (IOException e) {
			e.printStackTrace();
			return null;

		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException ex) {
			}
			try {
				bos.close();
			} catch (IOException ex) {
			}
		}
	}

}