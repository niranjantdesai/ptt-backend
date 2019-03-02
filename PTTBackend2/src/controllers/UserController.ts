import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import promise from "promise";

export class UserController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public addUser(userJSON) {
        return new promise<ResultInterface> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public getUser(userId: string) {
        print(userId);
        return new promise<ResultInterface> ((resolve, reject) => {
            try {
                
                this.User.findById(userId, (err, user) => {
                    if (err) {
                        print("err:", err);
                        reject({code: 400, result: "Bad request"});
                    } else {
                        if (user) {
                            user = moldJSON(user);
                            resolve({code: 200, result: user});
                        } else {
                            print("User not found:", userId);
                            reject({code: 404, result: "User not found"});
                        }
                    }
                });
            } catch (e) {
                print("500: server error:", e);
                let returnObject = {code: 500, result: "Server error"};
                reject(returnObject);
            }
        });
    }

    public updateUser(userId: string, updatedUser) {
        return new promise<ResultInterface> ((resolve, reject) => {
            try {
                
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public deleteUser(userId: string) {
        print(userId);
        return new promise<ResultInterface> ((resolve, reject) => {
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

export interface ResultInterface {
    code: number;
    result: any;
}