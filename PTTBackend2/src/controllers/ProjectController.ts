import mongoose from "mongoose";
import { UserSchema } from "../models/User";
import { ProjectSchema } from "../models/Project";
import { IDCounterController } from "./IDCounterController";
import promise from "promise";

export class ProjectController {
    User: mongoose.Model<mongoose.Document> = mongoose.model('User', UserSchema);
    Project: mongoose.Model<mongoose.Document> = mongoose.model('Project', ProjectSchema);
    counterController = new IDCounterController();

    constructor() {
        mongoose.set('useFindAndModify', false);
    }

    public addProject(userId: string, projectJSON: JSON) {
        return new promise<ProjectResultInterface> ((resolve, reject) => {

        });
    }

    public deleteProject(userId: string, projectId: string) {
        return new promise<ProjectResultInterface> ((resolve, reject) => {

        });
    }
}

function moldJSON(projectSchemaJSON) {
    let newObj = JSON.parse(JSON.stringify(projectSchemaJSON));
    delete newObj._id;
    delete newObj.sessions;
    return newObj;
}

function print(...a) {
    console.log(...a);
}

export interface ProjectResultInterface {
    code: number;
    result: any;
}