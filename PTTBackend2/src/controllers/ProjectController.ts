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

    public addProject(userId: string, projectJSON: JSON): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            
        });
    }

    public deleteProject(userId: string, projectId: string): promise<ProjectResultInterface> {
        return new promise<ProjectResultInterface> ((resolve, reject) => {
            
        });
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

export interface ProjectResultInterface {
    code: number;
    result: any;
}