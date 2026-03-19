const https = require('https');
const { spawn } = require('child_process');

// Fetch a brand new randomly generated Webhook Proxy URL from Smee
https.get('https://smee.io/new', (res) => {
  const smeeUrl = res.headers.location;
  
  console.log('\n\n======================================================');
  console.log('🔥 SUCCESS! PUBLIC WEBHOOK TUNNEL ESTABLISHED 🔥');
  console.log('======================================================\n');
  console.log('Copy and paste this EXACT URL into your GitHub Repository Webhook settings:');
  console.log(`👉  ${smeeUrl}  👈\n`);
  console.log('======================================================\n');
  
  // Forward payloads from that Smee URL directly to our Spring Boot backend
  const smeeProcess = spawn('npx', ['smee-client', '-u', smeeUrl, '-t', 'http://localhost:8080/api/webhooks/github'], { 
      stdio: 'inherit', 
      shell: true 
  });
  
}).on('error', (e) => {
  console.error("Failed to connect to Smee.io", e);
});
