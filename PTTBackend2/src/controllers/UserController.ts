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

    public addUser(userJSON): promise<UserResultInterface> {
        return new promise<UserResultInterface> ((resolve, reject) => {
            this.counterController.getNextUserId()
            .then(obj => {
                let userId = obj["result"];

                try {
                    userJSON.id = userId; // overwriting the ID with what our generator tells us
                    userJSON = this.removeAllButSomeKeys(userJSON, this.schemaKeys);
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
                    print("400:", error); // when all necessary keys are there but there are also some extra keys, shouldn't happen since all such keys were removed at the start
                    reject({code: 400, result: "Bad request"});
                }
            })
            .catch(obj => {
                print("500: server error:", obj);
                reject({code: 500, result: "Server error"});
            })
        });
    }

    public getUser(userId: string, hideKeys: boolean): promise<UserResultInterface> {
        return new promise<UserResultInterface> ((resolve, reject) => {
            try {
                let condition = { id: { $eq: userId } };
                this.User.findOne(condition, (err: any, user: mongoose.Document) => {
                    if (err) {
                        print("err:", err);
                        reject({code: 400, result: "Bad request"});
                    } else {
                        if (user) {
                            if (hideKeys) {
                                user = this.removeAllButSomeKeys(user, this.schemaKeys);
                            }
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

    public updateUser(userId: string, updatedUser): promise<UserResultInterface> {
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

    public deleteUser(userId: string): promise<UserResultInterface> {
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

    public getAllUsers(): promise<UserResultInterface> {
        return new promise<UserResultInterface> ((resolve ,reject) => {
            try {
                this.User.find((err: any, users: mongoose.Document[]) => {
                    if (err) {
                        print("err:", err);
                        reject({code: 400, result: "Bad request"});
                    } else {
                        let moldedUsers = users.map(user => this.removeAllButSomeKeys(user, this.schemaKeys));
                        resolve({code: 200, result: moldedUsers});
                    }
                });
            } catch (error) {
                print("500: server error:", error);
                reject({code: 500, result: "Server error"});
            }
        });
    }

    public appendProject(userId: string, projectId: string): promise<UserResultInterface> {
        return new promise <UserResultInterface> ((resolve, reject) => {
            let condition = { id: { $eq: userId } };
            let update = {$addToSet: { projects: { $each: [projectId] } }};
            let options = {new: true};
            this.User.findOneAndUpdate(condition, update, options, (err, user) => {
                if (err) {
                    print("err:", err);
                    reject({code: 400, result: "Bad request"});
                } else {
                    if (user) {
                        user = this.removeAllButSomeKeys(user, this.schemaKeys);
                        resolve({code: 201, result: user});
                    } else {
                        print(`A user with id: ${userId} not found`);
                        reject({code: 404, result: `User not found`});
                    }
                }
            });
        });
    }

    public removeProject(userId: string, projectId: string): promise<UserResultInterface> {
        return new promise <UserResultInterface> ((resolve, reject) => {
            let condition = { id: { $eq: userId } };
            let update = {$pull: { projects: { $in: [projectId] } }};;
            let options = {new: true};
            this.User.findOneAndUpdate(condition, update, options, (err, user) => {
                if (err) {
                    print("err:", err);
                    reject({code: 400, result: "Bad request"});
                } else {
                    if (user) {
                        user = this.removeAllButSomeKeys(user, this.schemaKeys);
                        resolve({code: 201, result: user});
                    } else {
                        print(`A user with id: ${userId} not found`);
                        reject({code: 404, result: `User not found`});
                    }
                }
            });
        });
    }

    private removeAllButSomeKeys(userSchemaJSON, keepWhichKeys: string[]) {
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