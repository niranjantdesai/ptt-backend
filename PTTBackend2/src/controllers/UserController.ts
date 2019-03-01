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
                delete userJSON.id; // ignoring the id field set by the frontend
                let newUser = new this.User(userJSON);
                newUser.save((error, user) => {
                    if (error) {
                        if (error.name === 'MongoError') {
                            if (error.code === 11000) {
                                print("duplicate emails");
                                reject({code: 409, result: error.errmsg});
                            } else {
                                print("unknown MongoError:", error);
                                reject({code: 500, result: "Server error"});
                            }
                        } else if (error.name === 'ValidationError') {
                            print("ValidationError:", error._message);
                            reject({code: 400, result: error._message});
                        }
                    } else {
                        user = moldJSON(user);
                        resolve({code: 201, result: user});
                    }
                });
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public getUser(userId: string) {
        return new promise<Result> ((resolve, reject) => {
            try {
                this.User.findById(userId, (err: any, user: mongoose.Document) => {
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
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public updateUser(userId: string, updatedUser) {
        return new promise<Result> ((resolve, reject) => {
            try {
                delete updatedUser.email; // ignoring any update to the email set by the frontend since modifying email is not allowed
                let condition = { _id: { $eq: userId } };
                let options = {new: true};
                this.User.findOneAndUpdate(condition, updatedUser, options, (err: any, res: mongoose.Document) => {
                    if (err) {
                        print("err:", err.message, err);
                        reject({code: 400, result: err.message});
                    } else {
                        if (res) {
                            res = moldJSON(res);
                            resolve({code: 200, result: res});
                        } else {
                            print("User not found:", userId);
                            reject({code: 404, result: "User not found"});
                        }
                    }
                })
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
                this.User.findByIdAndDelete(userId, (err: any, user: mongoose.Document) => {
                    if (err) {
                        print("err:", err.message);
                        reject({code: 400, result: err.message});
                    } else {
                        if (user) {
                            user = moldJSON(user);
                            resolve({code: 200, result: user});
                        } else {
                            print("User not found:", userId);
                            reject({code: 404, result: `User not found`});
                        }
                    }
                });
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