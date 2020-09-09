 /*
  *Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  *
  *WSO2 Inc. licenses this file to you under the Apache License,
  *Version 2.0 (the "License"); you may not use this file except
  *in compliance with the License.
  *You may obtain a copy of the License at
  *
  *http://www.apache.org/licenses/LICENSE-2.0
  *
  *Unless required by applicable law or agreed to in writing,
  *software distributed under the License is distributed on an
  *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *KIND, either express or implied.  See the License for the
  *specific language governing permissions and limitations
  *under the License.
  */

  const winston = require('winston')
  myFormat = () => {
   return new Date(Date.now()).toUTCString()
  }
  class LoggerService {
  constructor(route) {
   this.log_data = null
   this.route = route
   // create Logger
   const logger = winston.createLogger ({
   transports: [
   new winston.transports.Console(),
   new winston.transports.File({
   filename: `./logs/${route}.log`
   })
   ],
  format: winston.format.printf((info) => {
   let message = `${myFormat()} | ${info.level.toUpperCase()} | ${route}.log | ${info.message} | `
   message = info.obj ? message + `data:${JSON.stringify(info.obj)} | ` : message
   message = this.log_data ? message + `log_data:${JSON.stringify(this.log_data)} | ` : message
   return message
   })
   });
  this.logger = logger
   }
  setLogData(log_data) {
   this.log_data = log_data
   }
   async info(message) {
   this.logger.log('info', message);
   }
  async info(message, obj) {
   this.logger.log('info', message, {
   obj
   })
   }
  async debug(message) {
   this.logger.log('debug', message);
   }
  async debug(message, obj) {
   this.logger.log('debug', message, {
   obj
   })
   }
  async error(message) {
   this.logger.log('error', message);
   }
  async error(message, obj) {
   this.logger.log('error', message, {
   obj
   })
   }
  }
  module.exports = LoggerService
