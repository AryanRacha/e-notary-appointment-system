import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import SlotView from './pages/SlotView';
import BookingRequest from './pages/BookingRequest';
import StatusTracking from './pages/StatusTracking';
import ClientDashboard from './pages/ClientDashboard';
import NotaryDashboard from './pages/NotaryDashboard';
import { api } from './services/api';

export default function App() {
    const [currentPage, setCurrentPage] = useState('home');
    const [currentUser, setCurrentUser] = useState(null);
    const [selectedSlot, setSelectedSlot] = useState(null);
    const [trackingCode, setTrackingCode] = useState('');

    useEffect(() => {
        checkAuth();
    }, []);

    async function checkAuth() {
        try {
            const data = await api.getCurrentUser();
            if (data.authenticated && data.user) {
                setCurrentUser(data.user);
            }
        } catch (e) {
            console.error('Auth check error', e);
        }
    }

    async function handleLogout() {
        try {
            await api.logout();
            setCurrentUser(null);
            setCurrentPage('home');
        } catch (e) {
            console.error('Logout error', e);
        }
    }

    function handleLoginSuccess(user) {
        setCurrentUser(user);
        if (user.role === 'NOTARY') {
            setCurrentPage('notaryDashboard');
        } else {
            setCurrentPage('home');
        }
    }

    function handleSelectSlot(slot) {
        setSelectedSlot(slot);
        setCurrentPage('bookingRequest');
    }

    function handleTrackBooking(code) {
        setTrackingCode(code);
        setCurrentPage('track');
    }

    return (
        <div className="d-flex flex-column min-vh-100 bg-light">
            <Navbar
                currentUser={currentUser}
                onNavigate={setCurrentPage}
                onLogout={handleLogout}
                currentPage={currentPage}
            />

            <div className="flex-grow-1">
                {currentPage === 'home' && (
                    <Home onNavigate={setCurrentPage} currentUser={currentUser} />
                )}
                {currentPage === 'login' && (
                    <Login onLoginSuccess={handleLoginSuccess} onNavigate={setCurrentPage} />
                )}
                {currentPage === 'register' && (
                    <Register onNavigate={setCurrentPage} />
                )}
                {currentPage === 'slots' && (
                    <SlotView onSelectSlot={handleSelectSlot} onNavigate={setCurrentPage} currentUser={currentUser} />
                )}
                {currentPage === 'bookingRequest' && (
                    <BookingRequest
                        slot={selectedSlot}
                        currentUser={currentUser}
                        onNavigate={setCurrentPage}
                    />
                )}
                {currentPage === 'track' && (
                    <StatusTracking initialCode={trackingCode} />
                )}
                {currentPage === 'clientDashboard' && (
                    <ClientDashboard
                        currentUser={currentUser}
                        onNavigate={setCurrentPage}
                        onSelectBooking={handleTrackBooking}
                    />
                )}
                {currentPage === 'notaryDashboard' && (
                    <NotaryDashboard currentUser={currentUser} />
                )}
            </div>

            <Footer />
        </div>
    );
}
