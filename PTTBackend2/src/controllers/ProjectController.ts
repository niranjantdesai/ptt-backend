import mongoose from "mongoose";
import { ProjectSchema } from "../models/Project";
import { IDCounterController } from "./IDCounterController";
import { UserController } from "./UserController";
import promise from "promise";

export class ProjectController {
    Project: mongoose.Model<mongoose.Document> = mongoose.model('Project', ProjectSchema);
    counterController = new IDCounterController();
    userController = new UserController();
    schemaKeys = ["id", "projectname"];
    updatedableKeys = ["projectname"];

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public getProject(userId: string, projectId: string): promise<ProjectResultInterface> {
        return new promise <ProjectResultInterface> ((resolve, reject) => {
            this.userController.getUser(userId, false)
            .then(obj => {
                let user = obj["result"];
                let usersProjects = user["projects"];
                if (usersProjects.indexOf(projectId) == -1) {
                    print("User doesn't have this project");
                    reject({code: 404, result: "Project not found"});
                } else {
                    try {
                        let condition = { id: { $eq: projectId } };
                        this.Project.findOne(condition, (err: any, project: mongoose.Document) => {
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
                                    reject({code: 404, result: "Project not found"});
                                }
                            }
                        });
                    } catch (e) {
                        print("500: server error:", e);
                        reject({code: 500, result: "Server error"});
                    }
                }
            })
            .catch(obj => {
                reject(obj);
            });
        });
    }

    public addProject(userId: string, projectJSON: JSON): promise<ProjectResultInterface> {
        print("add project");
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
                                this.deleteProject(userId, projectId, false)
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
            })
        });
    }

    public deleteProject(userId: string, projectId: string, removeFromUsersListOfProjects: boolean): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            if (removeFromUsersListOfProjects) {
                this.userController.removeProject(userId, projectId)
                .then(__ => {
                    this.deleteProject(userId, projectId, false)
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

    public updateProject(userId: string, projectId: string, updatedProject: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
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

export interface ProjectResultInterface {
    code: number;
    result: any;
}