import { Injectable,EventEmitter } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CommonService {

  private linkStoreTransmitter = new EventEmitter;
  linkStoreReceiver = this.linkStoreTransmitter.asObservable();

  private sessionTranmitter = new EventEmitter;
  sessionReceiver = this.sessionTranmitter.asObservable();
  currentUrl = null;

  constructor(private httpClient:HttpClient) { }
  
  groupBy(array , f)
  {
		  var groups = {};
		  array.forEach( function( o )
		  {
		    var group = JSON.stringify( f(o) );
		    groups[group] = groups[group] || [];
		    groups[group].push( o );  
		  });
		  return Object.keys(groups).map( function( group )
		  {
		    return groups[group]; 
		  })
  }

  getPercentage(val,baseVal){
    var percentage = (val/baseVal) * 100;
    var percentage_fixed = percentage.toFixed(2);
    var value_percentage = percentage_fixed.split(".");
    if(parseInt(value_percentage[1]) > 0){
      return percentage.toFixed(2);
    }else{
      return value_percentage[0];
    }
      
  }

  hasPermission(permission){
    if(JSON.parse(localStorage.getItem("userPermissions"))) {
        let userPermissions = JSON.parse(localStorage.getItem("userPermissions"));
        if((userPermissions).find(up => up == permission)){
            return true;
        }else{
            return false;
        }
    }
    return false;
  }

  setIdentityServer(value:Boolean){
    this.linkStoreTransmitter.emit(value);
  }

  setCurrentUrl(url){
    this.currentUrl  = url;
  }
  
  getCurrentUrl(){
    return this.currentUrl;
  }

  setUserData(user){
    this.sessionTranmitter.emit(user);
  }

  getLogout():Observable<any>{
    return this.httpClient.get<any>(`${environment.appEndPoint}/logout`);
  }
}
