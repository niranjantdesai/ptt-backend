import mongoose from "mongoose";
const Schema = mongoose.Schema;
import { SessionSchema } from "./Session";

export const ProjectSchema = new Schema({
    id: { 
        type: Number,
        required: 'ID is required'
    },
    name: {
        type: String,
        required: 'Project name is required'
    },
    sessions: {
        type: [SessionSchema],
        default: []
    }
}, {
    versionKey: false,
    strict: "throw"
});