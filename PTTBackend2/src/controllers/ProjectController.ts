import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import { ProjectSchema } from "../models/Project";
import { IDCounterController } from "./IDCounterController";
import { UserController } from "./UserController";
import promise from "promise";

export class ProjectController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);
    Project: mongoose.Model<mongoose.Document> = mongoose.model('Project', ProjectSchema);
    counterController = new IDCounterController();
    userController = new UserController();
    relevantFields = ["id", "projectname"];

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public addProject(userId: string, projectJSON: JSON) {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            this.counterController.getNextProjectId()
            .then(obj => {
                let projectId = obj["result"];

            })
            .catch(obj => {
                print("500: server error:", obj);
                reject({code: 500, result: "Server error"});
            })
        });
    }

    public deleteProject(userId: string, projectId: string) {
        return new promise<ProjectResultInterface> ((resolve, reject) => {

        });
    }

    public removeIrrelevantKeys(userSchemaJSON) {
        let newObj = JSON.parse(JSON.stringify(userSchemaJSON));
        // delete newObj._id;
        // delete newObj.projects;
        // return newObj;
        let allKeys = Object.keys(newObj);
        allKeys.forEach((key) => {
            if (this.relevantFields.indexOf(key) == -1) {
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