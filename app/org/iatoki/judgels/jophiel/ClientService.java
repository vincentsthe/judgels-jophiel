package org.iatoki.judgels.jophiel;

import com.nimbusds.oauth2.sdk.AuthorizationCode;
import org.iatoki.judgels.commons.Page;

import java.util.List;

public interface ClientService {

    Client findClientById(long clientId);

    Client findClientByJid(String clientJid);

    AuthorizationCode generateAuthorizationCode(String clientJid, String URI, String responseType, String scope);

    String generateAccessToken(String code, String userId, String clientId, String scope);

    String generateRefreshToken(String code, String userId, String clientId, String scope);

    void generateIdToken(String code, String userId, String username, String clientId, String nonce, long authTime, String accessToken);

    org.iatoki.judgels.jophiel.AuthorizationCode findAuthorizationCodeByCode(String code);

    AccessToken findAccessTokenByCode(String code);

    RefreshToken findRefreshTokenByCode(String code);

    IdToken findIdTokenByCode(String code);

    long redeemAccessTokenById(long tokenId);

    void redeemRefreshTokenById(long tokenId);

    void redeemIdTokenById(long tokenId);

    void createClient(String name, String applicationType, List<String> scopes, List<String> redirectURIs);

    void updateClient(long clientId, String name, List<String> scopes, List<String> redirectURIs);

    void deleteClient(long clientId);

    Page<Client> pageClient(long page, long pageSize, String sortBy, String order, String filterString);

}
