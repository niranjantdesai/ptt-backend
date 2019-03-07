import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import promise from "promise";
import { IDCounterController } from "./IDCounterController";

export class UserController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);
    counterController = new IDCounterController();
    schemaKeys = ["id", "firstname", "lastname", "email"];
    updateableKeys = ["firstname", "lastname"];

    constructor() {mongoose.set('useFindAndModify', false);}

    public addUser(userJSON) {
        return new promise<UserResultInterface> ((resolve, reject) => {
            this.counterController.getNextUserId()
            .then(obj => {
                let userId = obj["result"];

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
                                print("ValidationError:", error);  // when some necessary field is absent
                                reject({code: 400, result: "Bad request"});
                            }
                        } else {
                            user = this.removeAllButSomeKeys(user, this.schemaKeys);
                            resolve({code: 201, result: user});
                        }
                    });
                } catch (error) {
                    print("400:", error); // when all necessary keys are there but there are also some extra keys
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
                            user = this.removeAllButSomeKeys(user, this.schemaKeys);
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
                // delete updatedUser.email; // ignoring any update to the email set by the frontend since modifying email is not allowed
                // delete updatedUser.id; // ignoring any update to the id set by the frontend since modifying id is not allowed
                // delete updatedUser.projects; // ignoring any update to the projects set by the frontend since modifying projects is not allowed
                updatedUser = this.removeAllButSomeKeys(updatedUser, this.updateableKeys);
                let condition = { id: { $eq: userId } };
                let options = {new: true};
                this.User.findOneAndUpdate(condition, updatedUser, options, (err: any, user: mongoose.Document) => {
                    if (err) {
                        print("err:", err);
                        reject({code: 400, result: "Bad request"});
                    } else {
                        if (user) {
                            user = this.removeAllButSomeKeys(user, this.schemaKeys);
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
                            user = this.removeAllButSomeKeys(user, this.schemaKeys);
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
                        let moldedUsers = users.map(user => this.removeAllButSomeKeys(user, this.schemaKeys));
                        resolve({code: 500, result: moldedUsers});
                    }
                });
            } catch (error) {
                print("500: server error:", error);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public appendProject() {

    }

    public removeAllButSomeKeys(userSchemaJSON, keepWhichKeys: string[]) {
        let newObj = JSON.parse(JSON.stringify(userSchemaJSON));
        // delete newObj._id;
        // delete newObj.projects;
        // return newObj;
        let allKeys = Object.keys(newObj);
        allKeys.forEach((key) => {
            if (keepWhichKeys.indexOf(key) == -1) {
                delete newObj[key];
            }
        });
        return newObj;
    }
}

function print(...a) {
    console.log(...a);
}

export interface UserResultInterface {
    code: number;
    result: any;
}