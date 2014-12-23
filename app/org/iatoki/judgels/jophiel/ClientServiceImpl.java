package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.jgit.util.StringUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.commons.Utilities;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.IdTokenModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;

import javax.persistence.NoResultException;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ClientServiceImpl implements ClientService {

    private ClientDao clientDao;
    private RedirectURIDao redirectURIDao;
    private AuthorizationCodeDao authorizationCodeDao;
    private AccessTokenDao accessTokenDao;
    private RefreshTokenDao refreshTokenDao;
    private IdTokenDao idTokenDao;

    public ClientServiceImpl(ClientDao clientDao, RedirectURIDao redirectURIDao, AuthorizationCodeDao authorizationCodeDao, AccessTokenDao accessTokenDao, RefreshTokenDao refreshTokenDao, IdTokenDao idTokenDao) {
        this.clientDao = clientDao;
        this.redirectURIDao = redirectURIDao;
        this.authorizationCodeDao = authorizationCodeDao;
        this.accessTokenDao = accessTokenDao;
        this.refreshTokenDao = refreshTokenDao;
        this.idTokenDao = idTokenDao;
    }

    @Override
    public Client findClientById(long clientId) {
        ClientModel clientModel = clientDao.findById(clientId);
        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType.toString(), scopeString, redirectURIs);
    }

    @Override
    public Client findClientByJid(String clientJid) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType.toString(), scopeString, redirectURIs);
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String clientJid, String redirectURI, String responseType, String scope) {
        try {
            ClientModel clientModel = clientDao.findByJid(clientJid);
            List<RedirectURIModel> redirectURIs = redirectURIDao.findByClientJid(clientJid);

            boolean check = true;
            int i = 0;
            List<String> enabledScopes = Arrays.asList(clientModel.scopes.split(","));
            String[] scopes = scope.split(" ");
            while ((check) && (i < scopes.length)) {
                if (!enabledScopes.contains(scopes[i].toUpperCase())) {
                    check = false;
                } else {
                    ++i;
                }
            }

            if ((responseType.equals("code")) && (redirectURIs.stream().filter(r -> r.redirectURI.equals(redirectURI)).count() >= 1) && (check)) {
                AuthorizationCode authorizationCode = new AuthorizationCode();

                AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();
                authorizationCodeModel.clientJid = clientJid;
                authorizationCodeModel.userJid = Utilities.getUserIdFromSession();
                authorizationCodeModel.code = authorizationCode.toString();
                authorizationCodeModel.expireTime = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2);
                authorizationCodeModel.redirectURI = redirectURI;
                authorizationCodeModel.scopes = scope;

                authorizationCodeDao.persist(authorizationCodeModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

                return authorizationCode;
            } else {
                return null;
            }
        } catch (NoResultException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String generateAccessToken(String code, String userId, String clientId, String scope) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();

        AccessTokenModel accessTokenModel1 = new AccessTokenModel();
        accessTokenModel1.code = code;
        accessTokenModel1.clientJid = clientId;
        accessTokenModel1.userJid = userId;
        accessTokenModel1.redeemed = false;
        accessTokenModel1.expireTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
        accessTokenModel1.scopes = scope;
        accessTokenModel1.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel1, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        return accessTokenModel1.token;
    }

    @Override
    public String generateRefreshToken(String code, String userId, String clientId, String scope) {
        com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken = new com.nimbusds.oauth2.sdk.token.RefreshToken();

        RefreshTokenModel refreshTokenModel1 = new RefreshTokenModel();
        refreshTokenModel1.code = code;
        refreshTokenModel1.clientJid = clientId;
        refreshTokenModel1.userJid = userId;
        refreshTokenModel1.redeemed = false;
        refreshTokenModel1.scopes = scope;
        refreshTokenModel1.token = refreshToken.getValue();

        refreshTokenDao.persist(refreshTokenModel1, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        return refreshTokenModel1.token;
    }

    @Override
    public void generateIdToken(String code, String userId, String username, String clientId, String nonce, long authTime, String accessToken) {
        try {
            byte[] encoded = Base64.decodeBase64("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDCdoHMwrsIiggV6hp7Yf4FZaKqkAeHuk5WAbBzuIDB40gQKKimwfKk+yaR6UKOOduGM3k4eDbaZy3n8NCkWnAvVIwt4rus7LhDhVUNrJGQU9BdK59x+wvhUtMcE2eP0V3hjeJmqzhoJxqLIAcnksU2Z3mmAkgbXecV16fCgo8G1Ny+Ai+FY2ZefRK+LF0u9rGQx5tA6XuQOUWvPJb45YlzmEDLwEMw7nOqwnnN6mSj9cKVfDX33ayvZY0aenEn7SMtrAkia5gBKGKDfN2KECX6OD9joatmNW0b+z9RtAXJvrWtkXhGaZR9+YBLBITllAtgkWMLWCCnDDOM4lNLoj9XAgMBAAECggEACPCz1Psa6DCYYJGLuCJwMEVU7iyC/B13noKjXx6bZM6TMJL99fSyuB0Hz+t+cNV+HzRcnVkBhJb7yE8M+JFj2Pk1HKLw5+lWK1yE5YUKiC0iRjZMNUxKZoiNRhwqRbVlcIo6X2f9xuQNV1oYmhwoTvEA6b3vHLr7dcidYNbpxnGMQZs035um6zShIFNqrmM4poQZZE9NbltOX1k/qxD0+OAAuemU3Y7WzH1XvTwXy7qU8O0PCktTe+QBSJZUPxy1nZwKbF1vdad39KfCjvxemkdUdzuPvlMfi+dsDXjAz71ukUO0r1+4n+l9DYOI8Pq6oI5ZGcwmz5B/Fd8RpPb2gQKBgQDy1o9HCnkL4rw3Wg6hkM46dlPPT7Mm5p+GrNbRxd6bX0wRpXivcasT60u4UZnG7gVVjpqour6tbyRaVNr5F6Cxg6YXDnZKwa8Jz64oUduQqMw7FvGtBG8+NR/26wI53Xoe1nq50ugkq3V3l9TtW9p0ccrsELP7Nu6Fmd4aa9AMFwKBgQDNALqptObo+2jODiuU4+w4wt/hUZa0BbmhjkhJNVpczZvUlXkLtMCq1ESxH4wWzRpBvIlcWpKnSyxzuFD5rtjqHh1kqVbFjQ2k0hRGs5S2vT+aC5oTH4M92nRPCZbWq+26jSVcvAgFj+S6MSOofMDYVOfM3dEKhzNKVsChjGsuwQKBgBCccrKWWc9hVCSpKWUN5b2ECJmexw97KSBqREuXMHIKY8a1PfsqWFyFdOmH03ATKhQ/K/8svwxYFPGE6nGtlxVtfvgGyjq04wdVyIEDkHRlx4qnOCLwsbdcpPIcA0v4BXmEjGKXtb+EZwWmQi92YAwlGI9rWRRvHoPPEa1XAKVDAoGALWgf8D71dl1ZVWqmFJB3Xgsr84hSzQUHnNUbBbwfi7au8WM6MHGUy0HBBUpriRFc43qTIjWdjhiEfA0zQlqMCS8qa4VmhtM7VmqBuzdDlUZNtB0lv16XfzfH00nYcywZt9xTjjrHvBOnIeaIc2VOgZwsy5/GEYLoxWp5uE6V3wECgYALHhV4lk4bH1Gm2S7Od8yPix62dbwoFMjfFiI4Y3dCu7Um93MS34OSWo2pixb9w+1Y/ZNNfrq+tEhUSsJKd3MvE8oskUR4bo4yMQJZC1+FSNUpehjz1Z9XiqJMpsl9GGYXo+nzU27PwlZdorgd8uiH30sNLcm9VG3e72hbQ0EpmQ==".getBytes("utf-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);

            JWSSigner signer = new RSASSASigner(privateKey);

            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(System.currentTimeMillis());

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject(username);
            claimsSet.setAudience(clientId);
            claimsSet.setIssuer("http://jophiel.judgels.org");
            claimsSet.setIssueTime(calendar.getTime());
            calendar.add(Calendar.WEEK_OF_MONTH, 2);
            claimsSet.setExpirationTime(calendar.getTime());
            claimsSet.setClaim("auth_time", authTime);
            claimsSet.setClaim("at_hash", JophielUtilities.hashMD5(accessToken).substring(accessToken.length() / 2));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(signer);

            IdTokenModel idTokenModel = new IdTokenModel();
            idTokenModel.userJid = userId;
            idTokenModel.clientJid = clientId;
            idTokenModel.code = code;
            idTokenModel.redeemed = false;
            idTokenModel.token = signedJWT.serialize();

            idTokenDao.persist(idTokenModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code) {
        AuthorizationCodeModel authorizationCodeModel = authorizationCodeDao.findByCode(code);

        return new org.iatoki.judgels.jophiel.AuthorizationCode(authorizationCodeModel);
    }

    @Override
    public AccessToken findAccessTokenByCode(String code) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByCode(code);

        return new AccessToken(accessTokenModel);
    }

    @Override
    public RefreshToken findRefreshTokenByCode(String code) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByCode(code);

        return new RefreshToken(refreshTokenModel);
    }

    @Override
    public IdToken findIdTokenByCode(String code) {
        IdTokenModel idTokenModel = idTokenDao.findByCode(code);

        return new IdToken(idTokenModel);
    }

    @Override
    public long redeemAccessTokenById(long tokenId) {
        AccessTokenModel accessTokenModel = accessTokenDao.findById(tokenId);
        if (accessTokenModel.redeemed) {
            throw new RuntimeException();
        }
        accessTokenModel.redeemed = true;
        accessTokenModel.expireTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14);

        accessTokenDao.edit(accessTokenModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        return TimeUnit.DAYS.toMillis(14);
    }

    @Override
    public void redeemRefreshTokenById(long tokenId) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findById(tokenId);
        if (refreshTokenModel.redeemed) {
            throw new RuntimeException();
        }
        refreshTokenModel.redeemed = true;

        refreshTokenDao.edit(refreshTokenModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
    }

    @Override
    public void redeemIdTokenById(long tokenId) {
        IdTokenModel idTokenModel = idTokenDao.findById(tokenId);
        if (idTokenModel.redeemed) {
            throw new RuntimeException();
        }
        idTokenModel.redeemed = true;

        idTokenDao.edit(idTokenModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
    }

    @Override
    public void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs) {
        ClientModel clientModel = new ClientModel();
        clientModel.name = name;
        clientModel.secret = JophielUtilities.hashMD5(UUID.randomUUID().toString());
        clientModel.applicationType = applicationType;
        List<String> scopeList = scopes.stream().filter(s -> (Scope.valueOf(s) != null)).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.persist(clientModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
        }
    }

    @Override
    public void updateClient(long clientId, String name, List<String> scopes, List<String> redirectURIs) {
        ClientModel clientModel = clientDao.findById(clientId);
        clientModel.name = name;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.edit(clientModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());

        List<RedirectURIModel> oldRedirectURIs = redirectURIDao.findByClientJid(clientModel.jid);
        for (RedirectURIModel redirectURIModel : oldRedirectURIs) {
            redirectURIDao.remove(redirectURIModel);
        }

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, Utilities.getUserIdFromSession(), Utilities.getIpAddressFromRequest());
        }
    }

    @Override
    public void deleteClient(long clientId) {
        ClientModel clientModel = clientDao.findById(clientId);

        clientDao.remove(clientModel);
    }

    @Override
    public Page<Client> pageClient(long page, long pageSize, String sortBy, String order, String filterString) {
        long totalPage = clientDao.countByFilter(filterString);
        List<ClientModel> clientModel = clientDao.findByFilterAndSort(filterString, sortBy, order, page * pageSize, pageSize);

        List<Client> clients = clientModel
                .stream()
                .map(c -> new Client(c.id, c.jid, c.name, c.secret, c.applicationType.toString(),
                        ImmutableSet.copyOf(c.scopes.split(",")),
                        ImmutableList.of())).collect(Collectors.toList());

        return new Page<>(clients, totalPage, page, pageSize);
    }

}
