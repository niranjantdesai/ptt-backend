import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import promise from "promise";

export class UserController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public addUser(userJSON) {
        return new promise<Result> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public getUser(userId: string) {
        return new promise<Result> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public updateUser(userId: string, updatedUser) {
        return new promise<Result> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public deleteUser(userId: string) {
        print(userId);
        return new promise<Result> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }
}

function moldJSON(mongoObject) {
    mongoObject = mongoObject.toJSON({ virtuals: true });
    delete mongoObject._id;
    return mongoObject;
}

function print(...a) {
    console.log(...a);
}

export interface Result {
    code: number;
    result: any;
}