import javax.crypto.Cipher
import javax.crypto.spec.{SecretKeySpec, IvParameterSpec}

object Encryption {
  def main(argv: Array[String]) {
    val iv = Array(0x30,0xd2,0xff,0x5d,0x08,0xac,0x83,0x95,0x02,0x0f,0x23,0x20,0x81,0xc9,0xc1,0xe4).map { _.toByte }
    val keyBytes = "1234567890ABCDEF1234567890ABCDEF".getBytes("UTF-8")
    val message = Array(0xb8,0x9f,0x27,0x30,0xe5,0x4d,0x81,0xf3,0xa9,0x3d,0x0b,0xe3,0xaa,0x52,0x50,0x15).map { _.toByte }
    val key = new SecretKeySpec(keyBytes, "AES")
    val cipher = Cipher.getInstance("AES/CBC/NoPadding")
    cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv))
    val result = cipher.doFinal(message)

    println(new String(result, "UTF-8"))
  }
}