import { check, sleep } from "k6";
import encoding from "k6/encoding";
import http from "k6/http";


export default function () {
  // When making a SOAP POST request we must not forget to set the content type to text/xml
  const creds = "dbUser:dbPass";
  const encodedCredentials = encoding.b64encode(creds);
  const options = {
    headers: {
      Authorization: `Basic ${encodedCredentials}`,
    },
  };
  const res = http.get(`http://localhost:8080/hello`, options);
  // Make sure the response is correct
  const passed = check(res, {
    "status is 200": (r) => r.status === 200,
  });
  if(!passed){
    console.error("Failed",res.status,res.body);
  }

  sleep(1);
}
