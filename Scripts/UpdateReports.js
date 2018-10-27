'use strict';
var functions = require('firebase-functions');
var admin = require('firebase-admin');
var request = require('request');
const fs = require('fs');
const { spawn } = require('child_process');
var pythonShell = require('python-shell');

var API_KEY = "AAAAy6R4AXo:APA91bEv7tqFhYcMvRPiA5x1JxkJ5R2p6NiE--DPhv6loNGE_ciBp2O1IWJlKwPxRRodwEMo77qy2ibADo0q6mIArtucl1lGgySzLUik5_ScoOCRN-q1EEWrTMigPXXmzNYcT1szkq03"; // Your Firebase Cloud Messaging Server API key
// Fetch the service account key JSON file contents
var serviceAccount = require("./serviceAccount.json");
// Initialize the app with a service account, granting admin privileges
admin.initializeApp({
	credential: admin.credential.cert(serviceAccount),
	databaseURL: "https://watcherth-96fe8.firebaseio.com/"
});

var ref = admin.database().ref();
module.exports = function (context, req) {
	var API_ID = '-sigi112Romeragroot';
	var url = 'https://api.sigimera.org/v1/crises?auth_token=-XB6vC77F4HLxqd-ms3F';
	var result;
	var olddata;
	var requests = ref.child('Reports');
	request.get({
		url: url,
		json: true,
		headers: {'User-Agent': 'request'}
	}, (err, res, data) => {
		if (err) {
			console.log('Error:', err);
		} else if (res.statusCode !== 200) {
			console.log('Status:', res.statusCode);
		} else {
			var c=0;
			var obj;
			for(var i = 0; i < data.length;i++){
				var exist = false;
				data[i].got_tweets = false;
				data[i] = getrelevantdata(data[i]);
				for(var j = 0; obj && j < obj.length;j++){
					if(obj[j].rf_link.valueOf() == data[i].rf_link.valueOf()){
						exist = true;
						break;
					}
				}
				if (!exist){
					requests.push(data[i]);
				}
			}
			const content = JSON.stringify(data);
			fs.writeFile("dataAdded.json", content, 'utf8', function (err) {
				if (err) {
					return console.log(err);
				}
				console.log("The file was saved!");
			});       
			console.log('Fetched API Successfully');
		}
	});
	const pyProg = spawn('D:/home/site/tools/python.exe', ['../ML/main.py']);
	pyProg.stdout.on('end', function(){
		var data = require('../ML/with_tweets.json');
		for(var i = 0; i < data.length;i++){
			data[i] = getrelevantdata(data[i]);
			requests.push(data[i]);
		}
	});

	context.done();
};

function getrelevantdata(data){
	var newdata = {};
	newdata.id = data._id;
	newdata.population = data.crisis_population_hash.value;
	newdata.pop_unit = data.crisis_population_hash.unit;
	if(data.gn_parentCountry.length>0){
		newdata.country = data.gn_parentCountry[0];
		newdata.country = newdata.country.replace(/^\w/, c => c.toUpperCase());
	}else{
		newdata.country = "";
	}
	var level = data.crisis_alertLevel;
	if(level == "Green"){
		newdata.alertLevel = "Low";
	} else if (level == "Red"){
		newdata.alertLevel = "High"; 
	} else {
		newdata.alertLevel = "Medium";
	}
	newdata.latitude = data.foaf_based_near[1];
	newdata.longitude = data.foaf_based_near[0];
	newdata.rf_link = data.rdfs_seeAlso;
	newdata.description = data.dc_description;
	newdata.short_des = data.dc_title;
	newdata.title = data.dc_subject[0];
	newdata.title = newdata.title.replace(/^\w/, c => c.toUpperCase());
	newdata.timestamp = data.dc_date;
	newdata.verified = true;
	newdata.isdeclined = false;
	newdata.utc_timestamp = 0.00;
	newdata.type = "Report"
	var cR = newdata.title;
	newdata.compactReport= {[cR] : ["Number of people affected: "+newdata.population + " "+ newdata.pop_unit,"Crisis Alert Level: "+newdata.alertLevel]};
	if(data.got_tweets == true){
		newdata.got_tweets = true;
		newdata.people = data.people;
		newdata.places = data.places;
		newdata.safe_longitude = data.safe_longitude;
		newdata.safe_latitude = data.safe_latitude; 
	}  
	else {
		newdata.got_tweets = false;
	}    
	return newdata;
}
