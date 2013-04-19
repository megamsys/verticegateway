import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.LoggerFactory;

import com.basho.riak.client.http.response.HttpResponse;

/* 
 ** Copyright [2012] [Megam Systems]
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 ** http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

/**
 * @author ram
 *
 */
public class MegamPlayHMAC {
	
 
		private final static String DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
		private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	 
		private final static String SECRET = "secretsecret";
		private final static String USERNAME = "jos";
	 
		private static final Logger LOG = (Logger) LoggerFactory.getLogger(MegamPlayHMAC.class);
	 
		public static void main(String[] args) throws HttpException, IOException, NoSuchAlgorithmException {
			MegamPlayHMAC client = new MegamPlayHMAC();
			client.makeHTTPCallUsingHMAC(USERNAME);
		}
	 
		public void makeHTTPCallUsingHMAC(String username) throws HttpException, IOException, NoSuchAlgorithmException {
			String contentToEncode = "{\"comment\" : {\"message\":\"blaat\" , \"from\":\"blaat\" , \"commentFor\":123}}";
			String contentType = "application/vnd.geo.comment+json";
			//String contentType = "text/plain";
			String currentDate = new SimpleDateFormat(DATE_FORMAT).format(new Date(0));
	 
			HttpPost post = new HttpPost("http://localhost:9000/v1/nodes");
			@SuppressWarnings("deprecation")
			StringEntity data = new StringEntity(contentToEncode,contentType,"UTF-8");
			post.setEntity(data);
	 
			String verb = post.getMethod();
			String contentMd5 = calculateMD5(contentToEncode);
			String toSign = verb + "\n" + contentMd5 + "\n"
					+ data.getContentType().getValue() + "\n" + currentDate + "\n"
					+ post.getURI().getPath();
	 
			String hmac = calculateHMAC(SECRET, toSign);
	 
			post.addHeader("hmac", "bob@example.com" + ":" + "secret");
			post.addHeader("Date", currentDate);
			post.addHeader("Content-Md5", contentMd5);
	 
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) client.execute(post);
	 
			System.out.println("client response:" + ((org.apache.http.HttpResponse) response).getStatusLine().getStatusCode());
		}
	 
		private String calculateHMAC(String secret, String data) {
			try {
				SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),	HMAC_SHA1_ALGORITHM);
				Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
				mac.init(signingKey);
				byte[] rawHmac = mac.doFinal(data.getBytes());
				String result = new String(Base64.encodeBase64(rawHmac));
				return result;
			} catch (GeneralSecurityException e) {
				((org.slf4j.Logger) LOG).warn("Unexpected error while creating hash: " + e.getMessage(),	e);
				throw new IllegalArgumentException();
			}
		}
	 
		private String calculateMD5(String contentToEncode) throws NoSuchAlgorithmException {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(contentToEncode.getBytes());
			String result = new String(Base64.encodeBase64(digest.digest()));
			return result;
		}


}
