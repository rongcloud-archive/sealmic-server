package cn.rongcloud.common;

/**
 * Created by weiqinxiao on 2019/2/27.
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import cn.rongcloud.utils.IdentifierUtils;
import cn.rongcloud.utils.SecurityUtils;
import com.alibaba.fastjson.JSON;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.data.redis.core.RedisTemplate;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenHelper {
    private static final String ISSUER = "rongcloud";
    private static final String CLAIM_MARK = "mark";
    private static final String CLAIM_DATA = "data";
    private String secret;
    private long tokenTTLInMilliSec;

    public JwtTokenHelper(String secret, long tokenTTLInMilliSec) {
        this.secret = secret;
        this.tokenTTLInMilliSec = tokenTTLInMilliSec;
    }

    public JwtTokenHelper(String secret) {
        this(secret, Long.MAX_VALUE);
    }

    /**
     * @param tokenTTLInMilliSec
     * @param tokenData
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public JwtToken createJwtToken(long tokenTTLInMilliSec, JwtUser tokenData) throws IOException {
        boolean neverExpire = -1 == tokenTTLInMilliSec;

        JwtToken token = new JwtToken();
        token.setUserId(tokenData.getUserId());
        token.setTokenId(IdentifierUtils.uuid());
        token.setIssuedTime(System.currentTimeMillis());
        token.setExpiredTime(neverExpire ? -1 : token.getIssuedTime() + tokenTTLInMilliSec);
        String mark = SecurityUtils.generateSalt(16);
        token.setToken(Jwts.builder().signWith(SignatureAlgorithm.HS256, getKey(secret, mark)).setIssuer(ISSUER)
                .setId(token.getTokenId()).setIssuedAt(new Date(token.getIssuedTime()))
                .setExpiration(neverExpire ? null : new Date(token.getExpiredTime())).claim(CLAIM_MARK, mark)
                .claim(CLAIM_DATA, JSON.toJSONString(tokenData)).compact());

        return token;
    }

    /**
     * @param tokenData
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public JwtToken createJwtToken(JwtUser tokenData) throws UnsupportedEncodingException, IOException {
        return createJwtToken(tokenTTLInMilliSec, tokenData);
    }

    public JwtUser checkJwtToken(String token, RedisTemplate<String, String> redisTemplate)
            throws ExpiredJwtException, MalformedJwtException, IOException {
        return JSON.parseObject((String) checkJwtTokenInternal(token, secret, redisTemplate).get(CLAIM_DATA),
                JwtUser.class);
    }

    /**
     * check jwt token and return payload
     *
     * @param token
     * @param secret
     * @param redisTemplate
     * @return
     * @throws ExpiredJwtException
     * @throws MalformedJwtException
     * @throws IOException
     */
    private static Claims checkJwtTokenInternal(String token, String secret,
                                                RedisTemplate<String, String> redisTemplate)
            throws ExpiredJwtException, MalformedJwtException, IOException {
        String[] contents = token.split("\\.");
        if (contents.length == 3) {
            Map<String, Object> map = JSON.parseObject(Base64.decodeBase64(contents[1]), Map.class);
            byte[] key = getKey(secret, (String) map.get(CLAIM_MARK));
            // check token
            Jws<Claims> jws = Jwts.parser().setSigningKey(key).requireIssuer(ISSUER).parseClaimsJws(token);
            return jws.getBody();
        }

        throw new MalformedJwtException("the token format is wrong!");
    }

    private static byte[] getKey(String secret, String nonce) throws UnsupportedEncodingException, IOException {
        return SecurityUtils.encryptHMAC(secret, nonce, SecurityUtils.HmacAlgorithm.HMAC_MD5);
    }
}