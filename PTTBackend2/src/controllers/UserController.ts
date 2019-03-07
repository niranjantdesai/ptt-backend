import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import promise from "promise";
import { IDCounterController } from "./IDCounterController";

export class UserController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);
    counterController = new IDCounterController();

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public addUser(userJSON) {
        return new promise<UserResultInterface> ((resolve, reject) => {
            let userId = null;
            this.counterController.getNextUserId()
            .then(obj => {
                userId = obj["result"];

                try {
                    userJSON.id = userId; // overwriting the ID with what our generator tells us
                    let newUser = new this.User(userJSON);
    
                    newUser.save((error, user) => {
                        if (error) {
                            if (error.name === 'MongoError') {
                                if (error.code === 11000) {
                                    print("duplicate emails", error);
                                    reject({code: 409, result: "Resource conflict"});
                                } else {
                                    print("unknown MongoError:", error);
                                    reject({code: 400, result: "Bad request"});
                                }
                            } else if (error.name === 'ValidationError') {
                                print("ValidationError:", error);
                                reject({code: 400, result: "Bad request"});
                            }
                        } else {
                            print(user);
                            user = moldJSON(user);
                            print(user);
                            resolve({code: 201, result: user});
                        }
                    });
                } catch (error) {
                    print("400:", error);
                    reject({code: 400, result: "Bad request"});
                }
            })
            .catch(obj => {
                print("500: server error:", obj);
                reject({code: 500, result: "Server error"});
            })
        });
    }

    public getUser(userId: string) {
        return new promise<UserResultInterface> ((resolve, reject) => {
            try {
                let condition = { id: { $eq: userId } };
                this.User.findOne(condition, (err: any, user: mongoose.Document) => {
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
        return new promise<UserResultInterface> ((resolve, reject) => {
            try {
                delete updatedUser.email; // ignoring any update to the email set by the frontend since modifying email is not allowed
                delete updatedUser.id; // ignoring any update to the id set by the frontend since modifying id is not allowed
                delete updatedUser.projects; // ignoring any update to the projects set by the frontend since modifying projects is not allowed
                let condition = { id: { $eq: userId } };
                let options = {new: true};
                this.User.findOneAndUpdate(condition, updatedUser, options, (err: any, user: mongoose.Document) => {
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
                })
            } catch (e) {
                print("500: server error:", e);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public deleteUser(userId: string) {
        print(userId);
        return new promise<UserResultInterface> ((resolve, reject) => {
            try {
                let condition = { id: { $eq: userId } };
                this.User.findOneAndDelete(condition, (err: any, user: mongoose.Document) => {
                    if (err) {
                        print("err:", err);
                        reject({code: 400, result: "Bad request"});
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

    public getAllUsers() {
        return new promise<UserResultInterface> ((resolve ,reject) => {
            try {
                this.User.find((err: any, users: mongoose.Document[]) => {
                    if (err) {
                        print("500: server error:", err)
                        reject({code: 500, result: "Server error"});
                    } else {
                        let moldedUsers = users.map(user => moldJSON(user));
                        resolve({code: 500, result: moldedUsers});
                    }
                });
            } catch (error) {
                print("500: server error:", error);
                reject({code: 500, result: "Server error"});
            }
        });
    }
}

function moldJSON(userSchemaJSON) {
    let newObj = JSON.parse(JSON.stringify(userSchemaJSON));
    delete newObj._id;
    delete newObj.projects;
    return newObj;
}

function print(...a) {
    console.log(...a);
}

export interface UserResultInterface {
    code: number;
    result: any;
}