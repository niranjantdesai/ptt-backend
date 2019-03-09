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
                                    project["userId"] = userId;
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
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.counterController.getNextProjectId()
            .then(obj => {
                let projectId = obj["result"];

                try{
                    projectJSON = this.removeAllButSomeKeys(projectJSON,this.schemaKeys);
                    projectJSON["id"] = projectId;
                    let newProject = new this.Project(projectJSON);

                    newProject.save((error,project) => {
                        if(error){
                            // reject
                            if (error.name === 'MongoError') {
                                if (error.code === 11000) {
                                    print("duplicate projectname", error);
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
                            project = this.removeAllButSomeKeys(project,this.schemaKeys);
                            this.userController.appendProject(userId,projectId)
                            .then(o1 => {
                                project["userId"] = userId;
                                resolve({code: 201,result: project});
                            })
                            .catch(o1 => {
                                this.deleteProject(userId,projectId,false)
                                .then(o2 => {
                                    reject(o1);
                                })
                                .catch(o2 => {
                                    reject(o1);
                                });
                            });
                        }
                    });
                } catch(error) {
                    print("500: server error:", error);
                    reject({code: 500, result: "Server error"});
                }
            })
            .catch(obj => {
                reject(obj);                
            });
        });
    }

    public deleteProject(userId: string, projectId: string, rmProject: boolean): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            if(rmProject){
                //check if user exists
                this.userController.getUser(userId,true)
                .then(o1 => {
                    //check if project belongs to this user
                    this.getProject(userId,projectId)
                    .then(o2 => {
                            this.userController.removeProject(userId,projectId)
                            .then(o3 => {
                                try{
                                    let condition = { id: { $eq: projectId } };
                                    this.Project.findOneAndDelete(condition,(error,project) => {
                                        if(error){
                                            //append the project back
                                            this.userController.appendProject(userId,projectId)
                                            .then(o4 => {
                                                print("error:", error);
                                                reject({code: 400, result: "Bad request"});
                                            })
                                            .catch(o4 => {
                                                print("error:", error);
                                                reject({code: 400, result: "Bad request"});
                                            });
                                        }else{
                                            if(project){
                                                project = this.removeAllButSomeKeys(project,this.schemaKeys);
                                                project["userId"] = userId;
                                                resolve({code: 200, result: project});
                                            } else {
                                                //not possible
                                                reject({code: 404, result: "Project not found"})
                                            }
                                        }
                                    });
                                }
                                catch(error){
                                    print("500: server error:", error);
                                    reject({code: 500, result: "Server error"});
                                }
                            })
                            .catch(o3 => {
                                reject(o3);
                            });
                    })
                    .catch(o2 => {
                        reject(o2);
                    });
                })
                .catch(o1 => {
                    reject(o1);
                });
            }else{
                try{
                    let condition = { id: { $eq: projectId } };
                    this.Project.findOneAndDelete(condition,(error,project) => {
                        if(error){
                            //append the project back
                            this.userController.appendProject(userId,projectId)
                            .then(o4 => {
                                print("error:", error);
                                reject({code: 400, result: "Bad request"});
                            })
                            .catch(o4 => {
                                print("error:", error);
                                reject({code: 400, result: "Bad request"});
                            });
                        }else{
                            if(project){
                                project = this.removeAllButSomeKeys(project,this.schemaKeys);
                                project["userId"] = userId;
                                resolve({code: 200, result: project});
                            } else {
                                //not possible
                                reject({code: 404, result: "Project not found"})
                            }
                        }
                    });
                }
                catch(error){
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