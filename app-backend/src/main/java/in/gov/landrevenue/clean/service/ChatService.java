package in.gov.landrevenue.clean.service;

import in.gov.landrevenue.clean.dto.chat.ChatResponse;
import in.gov.landrevenue.clean.entity.ChatMessage;
import in.gov.landrevenue.clean.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository repository;

    public ChatService(ChatMessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ChatResponse processMessage(String username, String userMessage) {
        repository.save(new ChatMessage(username, ChatMessage.SenderType.USER, userMessage));
        String reply = generateBotResponse(userMessage);
        ChatMessage botMsg = repository.save(new ChatMessage(username, ChatMessage.SenderType.BOT, reply));
        return new ChatResponse(botMsg.getId(), "BOT", reply, botMsg.getCreatedAt(), reply);
    }

    public List<ChatResponse> getHistory(String username) {
        return repository.findTop50ByUsernameOrderByCreatedAtAsc(username)
                .stream()
                .map(ChatResponse::fromMessage)
                .toList();
    }

    @Transactional
    public void clearHistory(String username) {
        repository.deleteByUsername(username);
    }

    public ChatResponse publicResponse(String message) {
        String reply = generateBotResponse(message);
        return new ChatResponse(null, "BOT", reply, java.time.LocalDateTime.now(), reply);
    }

    private String generateBotResponse(String message) {
        String q = message.toLowerCase();

        if (any(q, "hello", "hi", "hey", "start", "help", "namaste"))
            return "Hello! Welcome to LRMS Support. I can help you with registration, mutation, required documents, fees, and more. What would you like to know?";

        if (any(q, "status", "track", "where is my", "check my"))
            return "To check your registration status, open 'Registrations' in the sidebar and view your application. You also receive automatic SMS, email, and WhatsApp updates at every status change.";

        if (any(q, "document", "required", "needed", "bring", "submit", "what do i need"))
            return "Required documents for land registration:\n• Encumbrance Certificate (EC)\n• Property Tax Receipt\n• Patta / Title Deed copy\n• Aadhaar cards of buyer & seller\n• PAN cards of buyer & seller\n• Passport-size photos of all parties";

        if (any(q, "fee", "cost", "charge", "stamp duty", "how much", "amount", "payment"))
            return "Registration fees are calculated on the property's market value. Stamp duty is typically 5–7% of the value. Open 'Market Values' in the portal to see current district-wise rates before you register.";

        if (any(q, "otp", "not received", "resend", "didn't get", "no otp", "code"))
            return "If your OTP hasn't arrived:\n1. Confirm your mobile matches your Aadhaar record\n2. Wait 2–3 minutes and retry\n3. Check network connectivity\n4. Use 'Resend OTP' after 60 seconds\n\nFor Aadhaar OTP, the mobile must match UIDAI records exactly.";

        if (any(q, "mutation", "ownership", "transfer", "change owner", "property transfer"))
            return "To apply for a mutation:\n1. Go to 'Apply Mutation' in the sidebar\n2. Enter the land survey number and new owner details\n3. Attach the sale deed and supporting documents\n4. A Revenue Officer will review and approve the transfer.";

        if (any(q, "office", "hours", "timing", "contact", "sro", "registrar", "phone", "address", "location"))
            return "Sub-Registrar Office hours: Mon–Fri, 10:00 AM – 5:00 PM (closed on public holidays).\n\nMost services — encumbrance certificates, registration status, market values — are available online without visiting the office.";

        if (any(q, "encumbrance", " ec ", "certificate", "burden", "outstanding"))
            return "To get an Encumbrance Certificate (EC): Go to 'Encumbrance Cert' in the sidebar. Enter the survey number, district, and date range. EC shows all registered transactions on the property and is issued instantly for digitised records.";

        if (any(q, "market value", "valuation", "guideline value", "rate per sq"))
            return "District guideline values are set by state authorities and updated periodically. View current rates in 'Market Values'. These determine stamp duty for your registration.";

        if (any(q, "aadhaar", "aadhar", "citizen login", "citizen auth"))
            return "Citizen login uses Aadhaar OTP: enter your 12-digit Aadhaar on the login page and an OTP is sent to your Aadhaar-linked mobile number. The OTP is valid for 10 minutes.";

        if (any(q, "password", "forgot", "reset", "can't login", "login issue", "access"))
            return "Staff accounts: contact your system administrator to reset the password.\nCitizen accounts: use Aadhaar OTP — no password is needed.\n\nFor persistent issues, contact the LRMS helpdesk or your nearest SRO.";

        if (any(q, "cancel", "withdraw", "delete registration"))
            return "Once submitted for SRO approval, a registration cannot be cancelled from the portal. Contact your SRO office with the registration reference number to request cancellation.";

        if (any(q, "whatsapp", "sms", "notification", "update", "alert", "inform"))
            return "LRMS sends automatic SMS, email, and WhatsApp updates at every stage — draft submission, approval, rejection, and mutation. Ensure your mobile number and email address are correctly listed in your application.";

        if (any(q, "thank", "thanks", "thank you", "great", "bye", "goodbye", "ok"))
            return "You're welcome! Feel free to ask if you have more questions. Best of luck with your registration!";

        return "I didn't quite catch that. You can ask me about:\n• Registration process\n• Required documents\n• Stamp duty & fees\n• Mutation\n• OTP issues\n• Encumbrance Certificate\n• Market values\n• Office hours\n\nFor complex matters, please visit your nearest Sub-Registrar office.";
    }

    private boolean any(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
