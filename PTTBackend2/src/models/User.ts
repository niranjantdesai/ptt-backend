import mongoose from "mongoose";
const Schema = mongoose.Schema;

export const UserSchema = new Schema({
    id: { 
        type: Number,
        required: 'ID is required'
    },
    firstname: {
        type: String,
        required: 'First name is required'
    },
    lastname: {
        type: String,
        required: 'Last name is required'
    },
    email: {
        type: String,
        required: 'Email is required',
        unique: true
    },
    projects: {
        type: [{
            type: Number,
        }],
        default: []
    }
}, {
    versionKey: false,
    strict: "throw"
});

// // This will duplicate the _id to a field called id
// UserSchema.virtual('id').get(function() {
//     return this._id;
// });
