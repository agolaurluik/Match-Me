import { useState } from 'react';
import './RegisterModal.css';
import api from '../../api/api';
import { useNavigate } from 'react-router-dom';

function LoginModal({ onClose }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('')
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();



    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true)
        try {
            const data = await api.loginUser({ email, password });

            localStorage.setItem('Authorization', "Bearer " + data.token);

            // console.log('Login success:', data);
            onClose();
            navigate('/connections');
        } catch (error) {
            if (error.response && error.response.data && error.response.data.message) {
                setError(error.response.data.message);
            } else {
                setError(error.message || 'An unexpected error occurred');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className='modal-overlay' onDoubleClick={onClose}>
            <div className='modal-content' onClick={(e) => e.stopPropagation()}>
                <button className='modal-close' onClick={onClose}>X</button>
                <h2>Log in to Your Account</h2>
                <form className='register-form' onSubmit={handleLogin}>
                    <input
                        type='text'
                        placeholder='email'
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required />
                    <input
                        type='password'
                        placeholder='Password'
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required />
                    {error && <div className="error-text">{error}</div>}
                    <button type='submit' disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>
            </div>
        </div>
    )
}

export default LoginModal;