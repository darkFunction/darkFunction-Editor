/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package keygen;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.lang.StringBuffer;



/**
 *
 * @author s4m20
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {

        int a = 2;
      int b = "WKD".hashCode() % 3000;
      int c = "WWI".hashCode() % 3000;
      for (int i = 0; i <= c; i++)
         a = (a ^ i) % b;


        for (int i=0; i<2000; ++i)
        {
            String userID = Integer.toString(i);
            String fullNum = new String();
            if (userID.length() < 5)
            {               
                for (int j=0; j<5 - userID.length(); ++j)
                {
                    fullNum = fullNum + "0";
                }
                fullNum += userID.toString();
            }

            // now have userId eg 00000, 00001, 00002 (customer number)

            byte[] messageDigest = getKeyedDigest(fullNum.getBytes(), "dFE_ILikeSquirrels".getBytes());
            StringBuffer hexString = new StringBuffer();

            for (int j=0;j<messageDigest.length;j++)
            {
                String hex = Integer.toHexString(0xFF & messageDigest[j]);
                if(hex.length()==1)
                    hexString.append('0');
                hexString.append(hex);
            }

            
            String cutString = hexString.substring(0, 15);

            String str = cutString.toString().toUpperCase();

            StringBuffer sb = new StringBuffer(str);            
            sb.insert(2, fullNum.charAt(0));
            sb.insert(5, fullNum.charAt(1));
            sb.insert(6, fullNum.charAt(2));
            sb.insert(10, fullNum.charAt(3));
            sb.insert(13, fullNum.charAt(4));            
            sb.insert(5, " ");
            sb.insert(11, " ");
            sb.insert(17, " ");

            String replacedString = sb.toString();
            

            System.out.println(replacedString);
        }
    }

    public static byte[] getKeyedDigest(byte[] buffer, byte[] key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(buffer);
            return md5.digest(key);
        } catch (NoSuchAlgorithmException e) {}
    return null;
    }

}

