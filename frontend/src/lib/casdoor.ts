import axios from 'axios';
import { API_CONFIG } from '../config/api.config';

/**
 * Redirects user to Casdoor OAuth authorization portal with offline_access scope.
 */
export const redirectToCasdoorLogin = () => {
    const redirectUri = encodeURIComponent(window.location.origin + '/');
    const authUrl = `${API_CONFIG.CASDOOR.SERVER_URL}/login/oauth/authorize?client_id=${API_CONFIG.CASDOOR.CLIENT_ID}&response_type=code&redirect_uri=${redirectUri}&scope=openid%20profile%20email%20offline_access`;
    window.location.href = authUrl;
};

/**
 * Redirects user to Casdoor registration portal.
 */
export const redirectToCasdoorSignup = () => {
    const signupUrl = `${API_CONFIG.CASDOOR.SERVER_URL}/signup/${API_CONFIG.CASDOOR.APP_NAME}`;
    window.location.href = signupUrl;
};

/**
 * Exchanges OAuth authorization code for Access Token and Refresh Token.
 */
export const exchangeCasdoorCodeForToken = async (code: string): Promise<{ accessToken: string; refreshToken: string | null }> => {
    const params = new URLSearchParams();
    params.append('client_id', API_CONFIG.CASDOOR.CLIENT_ID);
    if (API_CONFIG.CASDOOR.CLIENT_SECRET) {
        params.append('client_secret', API_CONFIG.CASDOOR.CLIENT_SECRET);
    }
    params.append('grant_type', 'authorization_code');
    params.append('code', code);
    params.append('redirect_uri', window.location.origin + '/');

    const tokenUrl = `${API_CONFIG.CASDOOR.SERVER_URL}/api/login/oauth/access_token`;
    const response = await axios.post(tokenUrl, params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });

    const data = response.data;
    const accessToken = data.access_token || data.accessToken || data.token;
    const refreshToken = data.refresh_token || data.refreshToken || null;

    if (accessToken) {
        return { accessToken, refreshToken };
    }

    throw new Error(data.msg || 'Failed to exchange authorization code with Casdoor.');
};

/**
 * Direct Username & Password Token Fetch from Casdoor
 */
export const fetchCasdoorToken = async (username: string, password: string): Promise<{ accessToken: string; refreshToken: string | null }> => {
    const params = new URLSearchParams();
    params.append('client_id', API_CONFIG.CASDOOR.CLIENT_ID);
    if (API_CONFIG.CASDOOR.CLIENT_SECRET) {
        params.append('client_secret', API_CONFIG.CASDOOR.CLIENT_SECRET);
    }
    params.append('grant_type', 'password');
    params.append('username', username);
    params.append('password', password);
    params.append('scope', 'openid profile email offline_access');

    const tokenUrl = `${API_CONFIG.CASDOOR.SERVER_URL}/api/login/oauth/access_token`;
    const response = await axios.post(tokenUrl, params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });

    const data = response.data;
    const accessToken = data.access_token || data.accessToken || data.token;
    const refreshToken = data.refresh_token || data.refreshToken || null;

    if (accessToken) {
        return { accessToken, refreshToken };
    }

    throw new Error(data.msg || 'Casdoor authentication failed. Access token not received.');
};

/**
 * Refreshes an expired access token using the stored refresh_token.
 */
export const refreshCasdoorToken = async (refreshToken: string): Promise<{ accessToken: string; refreshToken: string | null }> => {
    const params = new URLSearchParams();
    params.append('client_id', API_CONFIG.CASDOOR.CLIENT_ID);
    if (API_CONFIG.CASDOOR.CLIENT_SECRET) {
        params.append('client_secret', API_CONFIG.CASDOOR.CLIENT_SECRET);
    }
    params.append('grant_type', 'refresh_token');
    params.append('refresh_token', refreshToken);

    const refreshUrl = `${API_CONFIG.CASDOOR.SERVER_URL}/api/login/oauth/refresh_token`;
    const response = await axios.post(refreshUrl, params, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    });

    const data = response.data;
    const accessToken = data.access_token || data.accessToken || data.token;
    const newRefreshToken = data.refresh_token || data.refreshToken || refreshToken;

    if (accessToken) {
        return { accessToken, refreshToken: newRefreshToken };
    }

    throw new Error(data.msg || 'Failed to refresh Casdoor access token.');
};
