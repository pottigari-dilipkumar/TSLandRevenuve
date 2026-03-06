const API_BASE = window.location.origin.replace(':8081', ':8080');
let verifiedToken = null;

async function post(path, body) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

async function get(path) {
  const res = await fetch(`${API_BASE}${path}`);
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

document.getElementById('sendOtp').onclick = async () => {
  const aadhaarNumber = document.getElementById('aadhaar').value.trim();
  const message = document.getElementById('otpMessage');
  try {
    const data = await post('/api/auth/aadhaar/send-otp', { aadhaarNumber });
    message.textContent = `${data.message}. Demo OTP: ${data.demoOtp}`;
    document.getElementById('otpPanel').classList.remove('hidden');
  } catch (e) {
    message.textContent = e.message;
  }
};

document.getElementById('verifyOtp').onclick = async () => {
  const aadhaarNumber = document.getElementById('aadhaar').value.trim();
  const otp = document.getElementById('otp').value.trim();
  const tokenLabel = document.getElementById('token');
  try {
    const data = await post('/api/auth/aadhaar/verify-otp', { aadhaarNumber, otp });
    verifiedToken = data.verifiedIdentityToken;
    tokenLabel.textContent = `Verified Token: ${verifiedToken}`;
  } catch (e) {
    tokenLabel.textContent = e.message;
  }
};

document.getElementById('submitRegistration').onclick = async () => {
  const payload = {
    parcelId: document.getElementById('parcelId').value.trim(),
    sellerName: document.getElementById('sellerName').value.trim(),
    buyerName: document.getElementById('buyerName').value.trim(),
    registrationRef: document.getElementById('registrationRef').value.trim(),
    deedHash: document.getElementById('deedHash').value.trim(),
    verifiedIdentityToken: verifiedToken || ''
  };
  const result = document.getElementById('registrationResult');
  try {
    const data = await post('/api/registrations', payload);
    result.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    result.textContent = e.message;
  }
};

document.getElementById('verifyRegistration').onclick = async () => {
  const registrationRef = document.getElementById('verifyRef').value.trim();
  const result = document.getElementById('verifyResult');
  try {
    const data = await get(`/api/public/verify/${encodeURIComponent(registrationRef)}`);
    result.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    result.textContent = e.message;
  }
};
