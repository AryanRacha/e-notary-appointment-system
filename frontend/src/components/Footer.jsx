import React from 'react';

export default function Footer() {
    return (
        <footer className="py-4 bg-white border-top mt-auto">
            <div className="container text-center text-muted small">
                <p className="mb-1">
                    <strong>Containerized E-Notary Appointment System</strong> &bull; Developed by <span>Aryan Racha</span> (Roll No: <span>23102C0055</span>)
                </p>
                <p className="mb-0 text-secondary">
                    Powered by React 18, Spring Boot 3 REST, Jenkins CI/CD, Docker &amp; Ansible
                </p>
            </div>
        </footer>
    );
}
