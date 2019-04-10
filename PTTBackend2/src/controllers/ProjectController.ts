import mongoose from "mongoose";
import { ProjectSchema } from "../models/Project";
import { SessionSchema } from "../models/Session";
import { IDCounterController } from "./IDCounterController";
import { UserController } from "./UserController";
import promise from "promise";
import { rejects } from "assert";

export class ProjectController {
    Project: mongoose.Model<mongoose.Document> = mongoose.model('Project', ProjectSchema);
    Session: mongoose.Model<mongoose.Document> = mongoose.model('Session', SessionSchema);

    counterController = new IDCounterController();
    userController = new UserController();
    schemaKeys = ["id", "projectname"];
    updatedableKeys = ["projectname"];
    exposeSessionKeys = ["id", "projectname", "sessions"];

    sessionSchemaKeys = ["id", "startTime", "endTime", "counter"];
    sessionUpdatedableKeys = ["startTime", "endTime", "counter"];
    sessionDateKeys = ["startTime", "endTime"];

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public getAllProjects(userId: string): promise<ProjectResultInterface> {
        return new promise <ProjectResultInterface> ((resolve, reject) => {
            this.userController.getUser(userId, false)
            .then(obj => {
                let user = obj["result"];
                let usersProjectIds = user["projects"];
                let usersProjects = [];
                let count = 0;

                if (usersProjectIds.length == 0) {
                    resolve({code: 200, result: usersProjects});
                } else {
                    for (let i = 0; i < usersProjectIds.length; i++) {
                        let projectId = usersProjectIds[i];

                        this.getProject(userId, projectId, true)
                        .then(obj => {
                            let aProject = obj["result"];
                            usersProjects.push(aProject);
                            count += 1;

                            if (count == usersProjectIds.length) {
                                resolve({code: 200, result: usersProjects});
                            }
                        })
                        .catch(obj => {
                            reject(obj);
                        })
                    }
                }
            })
            .catch(obj => {
                reject(obj);
            })
        });
    }

    public getProject(userId: string, projectId: string, hideSessions: boolean): promise<ProjectResultInterface> {
        return new promise <ProjectResultInterface> ((resolve, reject) => {
            this.userController.getUser(userId, false)
            .then(obj => {
                let user = obj["result"];
                let usersProjectIds = user["projects"];

                try {
                    let condition = { id: { $eq: projectId } };
                    this.Project.findOne(condition, (err: any, project: mongoose.Document) => {
                        if (err) {
                            print("err:", err);
                            reject({code: 400, result: "Bad request"});
                        } else {
                            if (project) {
                                project = this.removeAllButSomeKeys(project, this.exposeSessionKeys);
                                if (hideSessions) {
                                    project = this.removeAllButSomeKeys(project, this.schemaKeys);
                                }
                                project["userId"] = Number(userId);
                                if (usersProjectIds.indexOf(projectId) == -1) {
                                    print("User doesn't have this project");
                                    reject({code: 404, result: "Project not found"});
                                } else {
                                    resolve({code: 200, result: project});
                                }
                            } else {
                                print("Project not found:", projectId);
                                reject({code: 404, result: "Project not found"});
                            }
                        }
                    });
                } catch (e) {
                    print("500: server error:", e);
                    reject({code: 500, result: "Server error"});
                }
            })
            .catch(obj => {
                reject(obj);
            });
        });
    }

    public addProject(userId: string, projectJSON: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.counterController.getNextProjectId()
            .then(obj => {
                let projectId = obj["result"];

                try {
                    // overwriting the ID with what our generator tells us 
                    projectJSON = this.removeAllButSomeKeys(projectJSON, this.schemaKeys);
                    projectJSON["id"] = projectId;
                    
                    let newProject = new this.Project(projectJSON);
                    newProject.save((error, project) => {
                        if (error) {
                            if (error.name === 'MongoError') {
                                if (error.code === 11000) {
                                    print("duplicate project names", error);
                                    reject({code: 409, result: "Resource conflict"});
                                } else {
                                    print("unknown MongoError:", error);
                                    reject({code: 400, result: "Bad request"});
                                }
                            } else if (error.name === 'ValidationError') {
                                // when some necessary field is absent
                                print("ValidationError:", error); 
                                reject({code: 400, result: "Bad request"});
                            }
                        } else {
                            project = this.removeAllButSomeKeys(project, this.schemaKeys);
                            project["userId"] = Number(userId);
                            
                            // now that project object has been added, lets add the project id to the list of projects for this user
                            this.userController.appendProject(userId, projectId)
                            .then(__ => {
                                resolve({code: 201, result: project});
                            })
                            .catch(obj => { 
                                // since the project could not be added to the user's list of projects, we should also delete this project from our collection
                                this.deleteProject(userId, projectId, false, false)
                                .then(__ => {
                                    reject(obj);
                                })
                                .catch(__ => { 
                                    // should not happen since the project was just added and should exist
                                    reject(obj);
                                })
                            })
                        }
                    });
                } catch (error) {
                    // when all necessary keys are there but there are also some extra keys, shouldn't happen since all such keys were removed at the start
                    print("400:", error); 
                    reject({code: 400, result: "Bad request"});
                }
            })
            .catch(obj => {
                print("500: server error:", obj);
                reject({code: 500, result: "Server error"});
            });
        });
    }

    public deleteProject(userId: string, projectId: string, removeFromUsersListOfProjects: boolean, checkUserProjectRelation: boolean): promise<ProjectResultInterface> {
        if (checkUserProjectRelation) {
            return new promise<ProjectResultInterface> ((resolve, reject) => {
                this.getProject(userId, projectId, true)
                .then(__ => {
                    this.deleteProject(userId, projectId, removeFromUsersListOfProjects, false)
                    .then(result => {
                        resolve(result);
                    })
                    .catch(result => {
                        reject(result); 
                    });
                })
                .catch(obj => {
                    reject(obj);
                })
            });
        } else {
            return new promise<ProjectResultInterface> ((resolve, reject) => {
                if (removeFromUsersListOfProjects) {
                    this.userController.removeProject(userId, projectId)
                    .then(__ => {
                        this.deleteProject(userId, projectId, false, false)
                        .then(result => {
                            resolve(result);
                        })
                        .catch(result => {
                            // put the project back into the list of user's projects, so the DB is returned to its original state
                            this.userController.appendProject(userId, projectId)
                            .then(__ => {
                                reject(result); 
                            })
                            .catch(__ => {
                                // should not happen because removeProject didnt throw any error so appendProject should not either
                                reject(result); 
                            });
                        });
                    })
                    .catch(obj => { 
                        // could be that the user wasnt found or bad request
                        reject(obj);
                    })
                } else {
                    try {
                        let condition = { id: { $eq: projectId } };
                        this.Project.findOneAndDelete(condition, (err: any, project: mongoose.Document) => {
                            if (err) {
                                print("err:", err);
                                reject({code: 400, result: "Bad request"});
                            } else {
                                if (project) {
                                    project = this.removeAllButSomeKeys(project, this.schemaKeys);
                                    project["userId"] = Number(userId);
                                    resolve({code: 200, result: project});
                                } else {
                                    print("Project not found:", projectId);
                                    reject({code: 404, result: `Project not found`});
                                }
                            }
                        });
                    } catch (error) {
                        print("500: server error:", error);
                        reject({code: 500, result: "Server error"});
                    }
                }
            });
        }
    }

    public updateProject(userId: string, projectId: string, updatedProject: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.getProject(userId, projectId, true)
            .then(obj => {
                
                try {
                    updatedProject = this.removeAllButSomeKeys(updatedProject, this.updatedableKeys);
                    let condition = { id: { $eq: projectId } };
                    let options = {new: true};
                    this.Project.findOneAndUpdate(condition, updatedProject, options, (err: any, project: mongoose.Document) => {
                        if (err) {
                            if (err.name === 'MongoError') {
                                if (err.code === 11000) {
                                    print("duplicate project names", err);
                                    reject({code: 409, result: "Resource conflict"});
                                } else {
                                    print("unknown MongoError:", err);
                                    reject({code: 400, result: "Bad request"});
                                }
                            } else {
                                // when some necessary field is absent
                                print("ValidationError:", err); 
                                reject({code: 400, result: "Bad request"});
                            }
                        } else {
                            project = this.removeAllButSomeKeys(project, this.schemaKeys);
                            project["userId"] = Number(userId);
                            resolve({code: 200, result: project});
                        }
                    });
                } catch (e) {
                    print("500: server error:", e);
                    reject({code: 500, result: "Server error"});
                }
            })
            .catch(obj => {
                reject(obj);
            });
        });
    }

    public addSession(userId: string, projectId: string, newSession: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.counterController.getNextSessionId()
            .then(obj => {
                //Check if user actually has this project
                this.getProject(userId, projectId, true)
                .then(result => {
                    //if user actually has this project, try to insert a new session
                    newSession = this.removeAllButSomeKeys(newSession, this.sessionSchemaKeys);
                    let sessionId = obj["result"];

                    try {
                        newSession["id"] = sessionId;

                        if (!this.areDatesValid(newSession, this.sessionDateKeys)) {
                            print("Invalid dates!");
                            reject({code: 400, result: "Bad request"});
                        } else {
                            let sessionObj = new this.Session(newSession);
                            sessionObj.save((error, __) => {
                                if (error) {
                                    print("Error:", error);
                                    reject({code: 400, result: "Bad request"});
                                } else {
                                    newSession = this.removeAllButSomeKeys(newSession, this.sessionSchemaKeys);
                                    newSession = this.changeDateFormatForFields(newSession, this.sessionDateKeys);
                                    
                                    let filter = { id: { $eq: projectId } }
                                    let update = { $push: { sessions: newSession } };
                                    let options = { new: true };
                                    
                                    this.Project.findOneAndUpdate(filter, update, options, (err, updatedProject) => {
                                        if (err) {
                                            print("err:", err);
                                            reject({code: 400, result: "Bad Request"});
                                        } else {
                                            if (updatedProject) {
                                                // resolve not with the newSession JSON but with the actual session that has been added in the array
                                                let projectSessions = updatedProject["sessions"];

                                                let result = projectSessions.filter(aSession => aSession["id"] == sessionId);
                                                if (result.length == 0) {
                                                    print("500: server error, shouldn't happen");
                                                    reject({code: 500, result: "Server error"});
                                                } else if (result.length == 1) {
                                                    let session = this.removeAllButSomeKeys(result[0], this.sessionSchemaKeys);
                                                    session = this.changeDateFormatForFields(session, this.sessionDateKeys);
                                                    resolve({code: 201, result: session});
                                                } else {
                                                    print("500: server error, shouldn't happen");
                                                    reject({code: 500, result: "Server error"});
                                                }

                                            } else {
                                                print(`No Project with id: ${projectId}`);
                                                reject({code: 404, result: `Project ${projectId} Not Found`});
                                            }
                                        }
                                    });


                                }
                            });
                        }
                    } catch (error) {
                        print("500: server error:", error);
                        reject({code: 500, result: "Server error"});
                    }
                })
                .catch(result => {
                    reject(result);
                });
            })
            .catch(obj => {
                print("500: server error:", obj);
                reject({code: 500, result: "Server error"});
            });
        });
    }

    public updateSession(userId: string, projectId: string, sessionId: string, updatedSession: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.getProject(userId, projectId, true)
            .then(obj => {
                try {

                    let conditions = {id: projectId, "sessions.id": sessionId};
                    let attrUpdate = {};
                    updatedSession = this.removeAllButSomeKeys(updatedSession, this.sessionUpdatedableKeys);
                    Object.keys(updatedSession).forEach(key => {
                        attrUpdate[`sessions.$.${key}`] = updatedSession[key];
                    });

                    if (!this.areDatesValid(updatedSession, this.sessionDateKeys)) {
                        print("Invalid dates!");
                        reject({code: 400, result: "Bad request"});
                    } else {
                        let update = {$set: attrUpdate};
                        let options = {new: true};
                        this.Project.findOneAndUpdate(conditions, update, options)
                        .exec((err, updatedProject) => {
                            if (err) {
                                print("err:", err);
                                reject({code: 400, result: "Bad request"});
                            } else {
                                if (updatedProject) {
                                    let projectSessions = updatedProject["sessions"];
                                    let result = projectSessions.filter(session => session["id"] == sessionId);
                                    if (result.length == 0) {
                                        print("500: server error, shouldn't happen");
                                        reject({code: 500, result: "Server error"});
                                    } else if (result.length == 1) {
                                        let session = this.removeAllButSomeKeys(result[0], this.sessionSchemaKeys);
                                        session = this.changeDateFormatForFields(session, this.sessionDateKeys);
                                        resolve({code: 200, result: session});
                                    } else {
                                        print("500: server error, shouldn't happen");
                                        reject({code: 500, result: "Server error"});
                                    }
                                } else {
                                    print("Session not found:", sessionId);
                                    reject({code: 404, result: "Session not found"});
                                }
                            }
                        });
                    }

                } catch (e) {
                    print("500: server error:", e);
                    reject({code: 500, result: "Server error"});
                }
            })
            .catch(obj => {
                reject(obj);
            })

        });
    }

    public getReport(userId: string, projectId: string, from: string, to: string, includeCompletedPomodoros: boolean, includeTotalHoursWorkedOnProject: boolean): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.getProject(userId, projectId, false)
            .then(obj => {
                let usersProject = obj["result"];
                let usersSessions = usersProject["sessions"];

                let report = {"sessions": []};
                let sumCounter: number = 0;
                let totalTimeForProject: number = 0;

                let dummySession = {"startTime": from, "endTime": to};
                if (this.areDatesValid(dummySession, this.sessionDateKeys)) {
                    let fromDate = new Date(from);
                    let toDate = new Date(to);
                    if (toDate < fromDate) {
                        reject({code: 400, result: "Bad request"});
                    } else {
                        usersSessions.forEach(session => {
                            let sessionStartTime: string = session["startTime"];
                            let sessionEndTime: string = session["endTime"];
    
                            if (this.areDatesOverlapping(from, to, sessionStartTime, sessionEndTime)) {
                                let reportSession = {};
                                reportSession["startingTime"] = sessionStartTime;
                                reportSession["endingTime"] = sessionEndTime;
    
                                let sessionEndTimeDate = new Date(sessionEndTime);
                                let sessionStartTimeDate = new Date(sessionStartTime);
                                let hoursWorked: number = (sessionEndTimeDate.getTime() - sessionStartTimeDate.getTime()) / 3600000;
                                let hoursWorked2dp: Number = this.get2dpNumber(hoursWorked);
                                reportSession["hoursWorked"] = hoursWorked2dp;
                                report.sessions.push(reportSession);
    
                                sumCounter += session["counter"];
                            }
                            
                            let aSessionEndTimeDate = new Date(sessionEndTime);
                            let aSessionStartTimeDate = new Date(sessionStartTime);
                            let aSessionHoursWorked: number = (aSessionEndTimeDate.getTime() - aSessionStartTimeDate.getTime()) / 3600000;
                            totalTimeForProject += aSessionHoursWorked;
                        })
    
                        if (includeCompletedPomodoros) {
                            report["completedPomodoros"] = Math.floor(sumCounter);
                        }
    
                        if (includeTotalHoursWorkedOnProject) {
                            report["totalHoursWorkedOnProject"] = this.get2dpNumber(totalTimeForProject);
                        }
                        resolve({code: 200, result: report});
                    }
                } else {
                    print(`Some invalid date: either ${from} or ${to}`);
                    reject({code: 400, result: "Bad request"});
                }
            })
            .catch(obj => {
                reject(obj);
            })
        });
    }

    // // helper functions
    private get2dpNumber(num: number): Number {
        return + parseFloat((Math.round(num * 100) / 100).toString()).toFixed(2);
    }

    private areDatesOverlapping(start1: string, end1: string, start2: string, end2: string) {
        let startDate1 = new Date(start1);
        let endDate1 = new Date(end1);
        let startDate2 = new Date(start2);
        let endDate2 = new Date(end2);
        return ((startDate1 <= endDate2) && (endDate1 >= startDate2));
    }

    private removeAllButSomeKeys(JSONObj, keepWhichKeys: string[]) {
        let newObj = JSON.parse(JSON.stringify(JSONObj));
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

    private areDatesValid(session: any, fields: string[]) {
        try {
            this.changeDateFormatForFields(session, fields);
            return true;
        } catch (e) {
            return false;
        }
    }

    private changeDateFormatForFields(session: any, fields: string[]) {
        let newObj = JSON.parse(JSON.stringify(session));
        let allKeys = Object.keys(session);
        allKeys.forEach( key => {
            if (fields.indexOf(key) != -1) {
                let dateObj = new Date(session[key]);
                let dateString = dateObj.toISOString();
                dateString = dateString.replace(/:[0-9]{2}\.[0-9]{3,}/, '');
                newObj[key] = dateString;
            }
        });
        return newObj;
    }
}

function print(...a) {
    console.log(...a);
}

export interface ProjectResultInterface {
    code: number;
    result: any;
}