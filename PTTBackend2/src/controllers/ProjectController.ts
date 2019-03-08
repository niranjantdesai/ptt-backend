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
            
        });
    }

    public deleteProject(userId: string, projectId: string): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {

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