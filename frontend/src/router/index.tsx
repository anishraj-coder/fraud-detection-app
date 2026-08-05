import { createBrowserRouter } from 'react-router';
import { MainLayout } from '../components/layout/MainLayout';
import { ProtectedRoute } from '../components/layout/ProtectedRoute';
import { DashboardPage } from '../pages/DashboardPage';
import { OnboardingPage } from '../pages/OnboardingPage';
import { DepositPage } from '../pages/DepositPage';
import { HistoryPage } from '../pages/HistoryPage';
import { AccountPage } from '../pages/AccountPage';
import { LoginPage } from '../pages/LoginPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { ErrorBoundary } from '../components/layout/ErrorBoundary';

export const router = createBrowserRouter([
    {
        path: '/',
        element: <MainLayout />,
        errorElement: <ErrorBoundary />,
        children: [
            {
                element: <ProtectedRoute />,
                children: [
                    { index: true, element: <DashboardPage /> },
                    { path: 'onboard', element: <OnboardingPage /> },
                    { path: 'deposit', element: <DepositPage /> },
                    { path: 'history', element: <HistoryPage /> },
                    { path: 'account', element: <AccountPage /> }
                ]
            },
            { path: 'login', element: <LoginPage /> },
            { path: '*', element: <NotFoundPage /> }
        ]
    }
]);
