import { useMutation } from '@tanstack/react-query';
import { fetchCasdoorToken } from '../lib/casdoor';
import { useAuthStore } from '../store/useAuthStore';
import type { LoginCredentialsSchemaType } from '../lib/schemas';

export function useLoginWithCredentials() {
    const setTokens = useAuthStore((state) => state.setTokens);

    return useMutation({
        mutationFn: async (credentials: LoginCredentialsSchemaType) => {
            const { accessToken, refreshToken } = await fetchCasdoorToken(credentials.username, credentials.password);
            return { accessToken, refreshToken };
        },
        onSuccess: ({ accessToken, refreshToken }) => {
            setTokens(accessToken, refreshToken);
        }
    });
}
