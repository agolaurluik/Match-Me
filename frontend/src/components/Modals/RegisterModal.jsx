import { useState } from 'react';
import './RegisterModal.css';
import api from '../../api/api';
import { useNavigate } from 'react-router-dom';

function RegisterModal({ onClose, onRegisterSuccess }) {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
    });

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData(prev => ({
            ...prev,
            [e.target.name]: e.target.value
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords do not match");
            return;
        }

        setLoading(true);
        setError('');

        try {
            const data = await api.registerUser({
                username: formData.username,
                email: formData.email,
                password: formData.password
            });
            const token = 'Bearer ' + data.token;

            localStorage.setItem('Authorization', token);
            onRegisterSuccess(token);

            onClose();
            navigate('/connections');
        } catch (err) {
            if (err.response && err.response.data && err.response.data.error) {
                setError(err.response.data.error);
            } else {
                setError(err.message || "Registration failed");
            }
        } finally {
            setLoading(false);
        }
    }
    return (
        <div className='modal-overlay' onDoubleClick={onClose}>
            <div className='modal-content' onClick={(e) => e.stopPropagation()}>
                <button className='modal-close' onClick={onClose}>X</button>
                <h2 className='authentication-modal-header'>Create an Account</h2>
                <form className='register-form' onSubmit={handleSubmit}>
                    <input type='email' name='email' placeholder='Email'
                        value={formData.email} onChange={handleChange} required />
                    <input type='password' name='password' placeholder='Password'
                        value={formData.password} onChange={handleChange} required />
                    <input type='password' name='confirmPassword' placeholder='Confirm password'
                        value={formData.confirmPassword} onChange={handleChange} required />
                    <input type='text' name='username' placeholder='Username'
                        value={formData.username} onChange={handleChange} required maxLength={20} />
                    {error && <div className="error-text">{error}</div>}
                    <button type='submit' disabled={loading}>
                        {loading ? 'Registering...' : 'Register'}
                    </button>

                </form>
            </div>
        </div>
    )
}

export default RegisterModal;