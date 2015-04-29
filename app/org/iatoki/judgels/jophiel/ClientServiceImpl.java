package org.iatoki.judgels.jophiel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
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
import org.apache.commons.lang3.StringUtils;
import org.iatoki.judgels.commons.IdentityUtils;
import org.iatoki.judgels.commons.JudgelsUtils;
import org.iatoki.judgels.commons.Page;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AccessTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.AuthorizationCodeDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.ClientDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.IdTokenDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RedirectURIDao;
import org.iatoki.judgels.jophiel.models.daos.interfaces.RefreshTokenDao;
import org.iatoki.judgels.jophiel.models.domains.AccessTokenModel;
import org.iatoki.judgels.jophiel.models.domains.AuthorizationCodeModel;
import org.iatoki.judgels.jophiel.models.domains.ClientModel;
import org.iatoki.judgels.jophiel.models.domains.IdTokenModel;
import org.iatoki.judgels.jophiel.models.domains.RedirectURIModel;
import org.iatoki.judgels.jophiel.models.domains.RefreshTokenModel;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class ClientServiceImpl implements ClientService {

    private final ClientDao clientDao;
    private final RedirectURIDao redirectURIDao;
    private final AuthorizationCodeDao authorizationCodeDao;
    private final AccessTokenDao accessTokenDao;
    private final RefreshTokenDao refreshTokenDao;
    private final IdTokenDao idTokenDao;

    public ClientServiceImpl(ClientDao clientDao, RedirectURIDao redirectURIDao, AuthorizationCodeDao authorizationCodeDao, AccessTokenDao accessTokenDao, RefreshTokenDao refreshTokenDao, IdTokenDao idTokenDao) {
        this.clientDao = clientDao;
        this.redirectURIDao = redirectURIDao;
        this.authorizationCodeDao = authorizationCodeDao;
        this.accessTokenDao = accessTokenDao;
        this.refreshTokenDao = refreshTokenDao;
        this.idTokenDao = idTokenDao;
    }

    @Override
    public List<Client> findAll() {
        List<ClientModel> clientModels = clientDao.findAll();
        ImmutableList.Builder<Client> clientBuilder = ImmutableList.builder();
        for (ClientModel clientModel : clientModels) {
            Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
            List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
            List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

            clientBuilder.add(createClientFromModel(clientModel, scopeString, redirectURIs));
        }

        return clientBuilder.build();
    }

    @Override
    public List<Client> findAllClientByTerm(String term) {
        List<ClientModel> clientModels = clientDao.findSortedByFilters("id", "asc", term, ImmutableMap.of(), 0, -1);
        ImmutableList.Builder<Client> clientBuilder = ImmutableList.builder();

        for (ClientModel clientModel : clientModels) {
            Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
            List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
            List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

            clientBuilder.add(createClientFromModel(clientModel, scopeString, redirectURIs));
        }

        return clientBuilder.build();
    }

    @Override
    public boolean isClientAuthorized(String clientJid, List<String> scopes) {
        String userJid = IdentityUtils.getUserJid();
        Collections.sort(scopes);

        return authorizationCodeDao.checkIfAuthorized(clientJid, userJid, StringUtils.join(scopes, ","));
    }

    @Override
    public boolean isValidAccessTokenExist(String token) {
        // TODO check for access token expiry
        return accessTokenDao.existsByToken(token);
    }

    @Override
    public boolean clientExistByClientJid(String clientJid) {
        return clientDao.existsByJid(clientJid);
    }

    @Override
    public boolean clientExistByClientName(String clientName) {
        return clientDao.existByName(clientName);
    }

    @Override
    public Client findClientById(long clientId) throws ClientNotFoundException {
        ClientModel clientModel = clientDao.findById(clientId);
        if (clientModel != null) {
            Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
            List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
            List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

            return createClientFromModel(clientModel, scopeString, redirectURIs);
        } else {
            throw new ClientNotFoundException("Client not found.");
        }
    }

    @Override
    public Client findClientByJid(String clientJid) {
        ClientModel clientModel = clientDao.findByJid(clientJid);

        Set<String> scopeString = ImmutableSet.copyOf(clientModel.scopes.split(","));
        List<RedirectURIModel> redirectURIModels = redirectURIDao.findByClientJid(clientModel.jid);
        List<String> redirectURIs = redirectURIModels.stream().map(r -> r.redirectURI).collect(Collectors.toList());

        return createClientFromModel(clientModel, scopeString, redirectURIs);
    }

    @Override
    public AuthorizationCode generateAuthorizationCode(String clientJid, String redirectURI, String responseType, List<String> scopes, long expireTime) {
        Collections.sort(scopes);
        ClientModel clientModel = clientDao.findByJid(clientJid);
        List<RedirectURIModel> redirectURIs = redirectURIDao.findByClientJid(clientJid);

        List<String> enabledScopes = Arrays.asList(clientModel.scopes.split(","));
        int i = 0;
        boolean check = true;
        while ((check) && (i < scopes.size())) {
            if (!enabledScopes.contains(scopes.get(i).toUpperCase())) {
                check = false;
            } else {
                ++i;
            }
        }

        if ((responseType.equals("code")) && (redirectURIs.stream().filter(r -> r.redirectURI.equals(redirectURI)).count() >= 1) && (check)) {
            AuthorizationCode authorizationCode = new AuthorizationCode();

            AuthorizationCodeModel authorizationCodeModel = new AuthorizationCodeModel();
            authorizationCodeModel.clientJid = clientJid;
            authorizationCodeModel.userJid = IdentityUtils.getUserJid();
            authorizationCodeModel.code = authorizationCode.toString();
            authorizationCodeModel.expireTime = expireTime;
            authorizationCodeModel.redirectURI = redirectURI;
            authorizationCodeModel.scopes = StringUtils.join(scopes, ",");
            authorizationCodeDao.persist(authorizationCodeModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

            return authorizationCode;
        } else {
            throw new IllegalStateException("Response type, redirect URI, or scope is invalid");
        }
    }

    @Override
    public String generateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel1 = new AccessTokenModel();
        accessTokenModel1.code = code;
        accessTokenModel1.clientJid = clientId;
        accessTokenModel1.userJid = userId;
        accessTokenModel1.redeemed = false;
        accessTokenModel1.expireTime = expireTime;
        accessTokenModel1.scopes = StringUtils.join(scopes, ",");
        accessTokenModel1.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel1, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return accessTokenModel1.token;
    }

    @Override
    public void generateRefreshToken(String code, String userId, String clientId, List<String> scopes) {
        com.nimbusds.oauth2.sdk.token.RefreshToken refreshToken = new com.nimbusds.oauth2.sdk.token.RefreshToken();
        Collections.sort(scopes);

        RefreshTokenModel refreshTokenModel1 = new RefreshTokenModel();
        refreshTokenModel1.code = code;
        refreshTokenModel1.clientJid = clientId;
        refreshTokenModel1.userJid = userId;
        refreshTokenModel1.redeemed = false;
        refreshTokenModel1.scopes = StringUtils.join(scopes, ",");
        refreshTokenModel1.token = refreshToken.getValue();

        refreshTokenDao.persist(refreshTokenModel1, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void generateIdToken(String code, String userId, String clientId, String nonce, long authTime, String accessToken, long expireTime) {
        try {
            byte[] encoded = Base64.decodeBase64(JophielProperties.getInstance().getIdTokenPrivateKey().getBytes("utf-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);

            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claimsSet = new JWTClaimsSet();
            claimsSet.setSubject(userId);
            claimsSet.setAudience(clientId);
            claimsSet.setIssuer(JophielProperties.getInstance().getJophielBaseUrl());
            claimsSet.setIssueTime(new Date(System.currentTimeMillis()));
            claimsSet.setExpirationTime(new Date(expireTime));
            claimsSet.setClaim("auth_time", authTime);
            claimsSet.setClaim("at_hash", JudgelsUtils.hashMD5(accessToken).substring(accessToken.length() / 2));

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS512), claimsSet);
            signedJWT.sign(signer);

            IdTokenModel idTokenModel = new IdTokenModel();
            idTokenModel.userJid = userId;
            idTokenModel.clientJid = clientId;
            idTokenModel.code = code;
            idTokenModel.redeemed = false;
            idTokenModel.token = signedJWT.serialize();

            idTokenDao.persist(idTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code) {
        AuthorizationCodeModel authorizationCodeModel = authorizationCodeDao.findByCode(code);

        return new org.iatoki.judgels.jophiel.AuthorizationCode(authorizationCodeModel);
    }

    @Override
    public AccessToken regenerateAccessToken(String code, String userId, String clientId, List<String> scopes, long expireTime) {
        com.nimbusds.oauth2.sdk.token.AccessToken accessToken = new BearerAccessToken();
        Collections.sort(scopes);

        AccessTokenModel accessTokenModel1 = new AccessTokenModel();
        accessTokenModel1.code = code;
        accessTokenModel1.clientJid = clientId;
        accessTokenModel1.userJid = userId;
        accessTokenModel1.redeemed = false;
        accessTokenModel1.expireTime = expireTime;
        accessTokenModel1.scopes = StringUtils.join(scopes, ",");
        accessTokenModel1.token = accessToken.getValue();

        accessTokenDao.persist(accessTokenModel1, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return new AccessToken(accessTokenModel1);
    }

    @Override
    public AccessToken findAccessTokenByAccessToken(String token) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByToken(token);

        return new AccessToken(accessTokenModel);
    }

    @Override
    public AccessToken findAccessTokenByCode(String code) {
        AccessTokenModel accessTokenModel = accessTokenDao.findByCode(code);

        return new AccessToken(accessTokenModel);
    }

    @Override
    public RefreshToken findRefreshTokenByRefreshToken(String token) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findByToken(token);

        return new RefreshToken(refreshTokenModel);
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

        accessTokenDao.edit(accessTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        return TimeUnit.SECONDS.convert((accessTokenModel.expireTime - System.currentTimeMillis()), TimeUnit.MILLISECONDS);
    }

    @Override
    public void redeemRefreshTokenById(long tokenId) {
        RefreshTokenModel refreshTokenModel = refreshTokenDao.findById(tokenId);
        if (refreshTokenModel.redeemed) {
            throw new RuntimeException();
        }
        refreshTokenModel.redeemed = true;

        refreshTokenDao.edit(refreshTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void redeemIdTokenById(long tokenId) {
        IdTokenModel idTokenModel = idTokenDao.findById(tokenId);
        if (idTokenModel.redeemed) {
            throw new RuntimeException();
        }
        idTokenModel.redeemed = true;

        idTokenDao.edit(idTokenModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
    }

    @Override
    public void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs) {
        ClientModel clientModel = new ClientModel();
        clientModel.name = name;
        clientModel.secret = JudgelsUtils.generateNewSecret();
        clientModel.applicationType = applicationType;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.persist(clientModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void updateClient(long clientId, String name, List<String> scopes, List<String> redirectURIs) {
        ClientModel clientModel = clientDao.findById(clientId);
        clientModel.name = name;
        List<String> scopeList = scopes.stream().filter(s -> ((s != null) && (Scope.valueOf(s) != null))).collect(Collectors.toList());
        clientModel.scopes = StringUtils.join(scopeList, ",");

        clientDao.edit(clientModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());

        List<RedirectURIModel> oldRedirectURIs = redirectURIDao.findByClientJid(clientModel.jid);
        for (RedirectURIModel redirectURIModel : oldRedirectURIs) {
            redirectURIDao.remove(redirectURIModel);
        }

        for (String redirectURI : redirectURIs) {
            RedirectURIModel redirectURIModel = new RedirectURIModel();
            redirectURIModel.redirectURI = redirectURI;
            redirectURIModel.clientJid = clientModel.jid;

            redirectURIDao.persist(redirectURIModel, IdentityUtils.getUserJid(), IdentityUtils.getIpAddress());
        }
    }

    @Override
    public void deleteClient(long clientId) {
        ClientModel clientModel = clientDao.findById(clientId);

        clientDao.remove(clientModel);
    }

    @Override
    public Page<Client> pageClients(long pageIndex, long pageSize, String orderBy, String orderDir, String filterString) {
        long totalPages = clientDao.countByFilters(filterString, ImmutableMap.of());
        List<ClientModel> clientModels = clientDao.findSortedByFilters(orderBy, orderDir, filterString, ImmutableMap.of(), pageIndex * pageSize, pageSize);

        List<Client> clients = Lists.transform(clientModels, m -> createClientFromModel(m, ImmutableSet.copyOf(m.scopes.split(",")), ImmutableList.of()));

        return new Page<>(clients, totalPages, pageIndex, pageSize);
    }

    private Client createClientFromModel(ClientModel clientModel, Set<String> scopeString, List<String> redirectURIs) {
        return new Client(clientModel.id, clientModel.jid, clientModel.name, clientModel.secret, clientModel.applicationType.toString(), scopeString, redirectURIs);
    }
}
