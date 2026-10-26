// Centralized API client for Spring Boot REST endpoints
const BASE_URL = '';

async function request(url, options = {}) {
    options.credentials = 'same-origin';
    options.headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    const response = await fetch(BASE_URL + url, options);
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(data.error || data.message || `Request failed with status ${response.status}`);
    }
    return data;
}

export const api = {
    // Auth & Users
    getCurrentUser: () => request('/api/auth/current'),
    login: (email, password) => request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
    }),
    register: (form) => request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(form)
    }),
    logout: () => request('/api/auth/logout', { method: 'POST' }),
    getNotaries: () => request('/api/notaries'),

    // Slots & Availability
    getSlots: (notaryId, date) => request(`/api/slots?notaryId=${notaryId || ''}&date=${date || ''}`),
    generateSlots: (payload) => request('/api/slots/generate', {
        method: 'POST',
        body: JSON.stringify(payload)
    }),

    // Bookings & Tracking
    createBooking: (payload) => request('/api/bookings', {
        method: 'POST',
        body: JSON.stringify(payload)
    }),
    trackBooking: (code) => request(`/api/bookings/track/${encodeURIComponent(code)}`),
    getMyBookings: () => request('/api/bookings/my'),
    getNotaryBookings: () => request('/api/bookings/notary'),
    confirmBooking: (id) => request(`/api/bookings/${id}/confirm`, { method: 'POST' }),
    cancelBooking: (id, cancellationReason) => request(`/api/bookings/${id}/cancel`, {
        method: 'POST',
        body: JSON.stringify({ cancellationReason })
    })
};
