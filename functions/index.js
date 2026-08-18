const functions = require("firebase-functions");
const twilio = require("twilio");

// Twilio credentials
const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const client = twilio(accountSid, authToken);

// SOS function
exports.sendSOS = functions.https.onRequest(async (req, res) => {
    try {
        // Expect body: { message: "SOS message", contacts: ["+60123456789", ...] }
        const { message, contacts } = req.body;

        if (!message || !contacts || contacts.length === 0) {
            res.status(400).send("Missing message or contacts");
            return;
        }

        for (let number of contacts) {
            if (number === "+16108908259") { 
                // Skip sending to your Twilio number itself
                continue;
            }
            await client.messages.create({
                body: message,
                from: "+16108908259", // Your Twilio number
                to: number
            });
        }

        res.status(200).send("SMS sent successfully");
    } catch (error) {
        console.error(error);
        res.status(500).send("Error sending SMS");
    }
});

// Force redeploy
