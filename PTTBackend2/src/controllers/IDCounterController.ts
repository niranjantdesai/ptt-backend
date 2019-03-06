import mongoose from "mongoose";
import { IDCounterSchema } from "../models/IDCounter";
import promise from "promise";

export class IDCounterController {
    counter: mongoose.Model<mongoose.Document> = mongoose.model('IDCounter', IDCounterSchema);

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public initializeDB() {
        return new promise<null> ((resolve, reject) => {
            let defaultCounters = new this.counter({});
            defaultCounters.save((error, counters) => {
                resolve(null);
            });
        })
    }

    public getNextUserId(){
        return new promise<IDResultInterface> ((resolve, reject) => {
            let condition = { name: { $eq: "counters" } };
            let update =  { $inc: { userId: 1 } };
            let options = {new: true};
            this.counter.findOneAndUpdate(condition, update, options, (err: any, counters: mongoose.Document) => {
                if (err) {
                    print(err);
                    reject({result: err});
                } else {
                    resolve({result: counters["userId"]});
                }
            });
        });
    }

    public getNextProjectId(){
        return new promise<IDResultInterface> ((resolve, reject) => {
            let condition = { name: { $eq: "counters" } };
            let update =  { $inc: { projectId: 1 } };
            let options = {new: true};
            this.counter.findOneAndUpdate(condition, update, options, (err: any, counters: mongoose.Document) => {
                if (err) {
                    print(err);
                    reject({result: err});
                } else {
                    resolve({result: counters["projectId"]});
                }
            });
        });
    }

    public getNextSessionId(){
        return new promise<IDResultInterface> ((resolve, reject) => {
            let condition = { name: { $eq: "counters" } };
            let update =  { $inc: { sessionId: 1 } };
            let options = {new: true};
            this.counter.findOneAndUpdate(condition, update, options, (err: any, counters: mongoose.Document) => {
                if (err) {
                    print(err);
                    reject({result: err});
                } else {
                    resolve({result: counters["sessionId"]});
                }
            });
        });
    }

}

function print(...a) {
    console.log(...a);
}

export interface IDResultInterface {
    result: any;
}