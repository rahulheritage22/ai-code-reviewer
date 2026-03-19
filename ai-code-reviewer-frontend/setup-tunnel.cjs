const https = require('https');
const { spawn } = require('child_process');

https.get('https://smee.io/new', (res) => {
  const smeeUrl = res.headers.location;
  
  console.log('\n\n======================================================');
  console.log('YOUR GITHUB WEBHOOK URL (COPY THIS INTO GITHUB):');
  console.log(smeeUrl);
  console.log('======================================================\n');
  
  const smeeProcess = spawn('npx', ['smee-client', '-u', smeeUrl, '-t', 'http://localhost:8080/api/webhooks/github'], { 
      stdio: 'inherit', 
      shell: true,
      detached: true
  });
  smeeProcess.unref(); // Allows the parent script to exit while Smee client stays alive in background
  
}).on('error', (e) => {
  console.error("Failed to connect to Smee.io", e);
});
