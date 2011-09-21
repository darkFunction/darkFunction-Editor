/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dfEditor.license;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.lang.StringBuffer;
/**
 *
 * @author s4m20
 */
public class LicenseReader
{
    public static boolean checkLicense(final String aLicense)
    {
        if (aLicense == null)
            return false;

        String license = aLicense.trim();

        // remove spaces
        license = license.replaceAll(" ", "");

        if (license.length() != 20)
            return false;
       
        String userID = new String();
        userID += license.charAt(2);
        userID += license.charAt(5);
        userID += license.charAt(6);
        userID += license.charAt(10);
        userID += license.charAt(13);

                   
        byte[] messageDigest = getKeyedDigest(userID.getBytes(), "dFE_ILikeSquirrels".getBytes());
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
        sb.insert(2, userID.charAt(0));
        sb.insert(5, userID.charAt(1));
        sb.insert(6, userID.charAt(2));
        sb.insert(10, userID.charAt(3));
        String replacedString =
        sb.insert(13, userID.charAt(4)).toString();

        

        if (license.equals(replacedString))
            return true;

        return false;
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

