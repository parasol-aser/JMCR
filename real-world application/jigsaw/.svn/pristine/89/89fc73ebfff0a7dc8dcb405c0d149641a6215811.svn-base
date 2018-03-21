package org.w3c.tools.jpeg;

import java.util.Hashtable;

/**
 * The EXIF decoder interface.
 *
 * <p>Special purpose field decoders for use by the Exif class must implement
 * this interface.</p>
 *
 * @version $Revision: 1.1 $
 * @author  Norman Walsh
 * @see Exif
 */
public interface TagDecoder {
  /** Decodes a field from the EXIF data and possibly augments the exif hash.
   *
   * @param exif The hash of field name/value pairs. This method should update
   *             this has with information extracted from data.
   * @param data The EXIF data.
   * @param format The EXIF format value for the field to be decoded.
   * @param offset The offset of the start of the field in the EXIF data.
   * @param length The length of the field.
   */
  public void decode(Hashtable exif, ExifData data, int format, int offset, int length);
}
