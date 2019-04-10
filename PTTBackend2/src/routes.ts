import express from 'express';
import { UserController } from "./controllers/UserController";
import { ProjectController } from "./controllers/ProjectController";

export const routes = express.Router();

let userController = new UserController();
let projectController = new ProjectController();
const baseUrl = '/ptt';

routes.route(`${baseUrl}/`)
.get((req, res) => {res.send('Backend Server is working');});

routes.route(`${baseUrl}/users`)
.get((req, res) => {
    userController.getAllUsers()
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.post((req, res) => {
    userController.addUser(req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId`)
.get((req, res) => {
    userController.getUser(req.params["userId"], true)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.put((req, res) => {
    userController.updateUser(req.params["userId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.delete((req, res) => {
    userController.deleteUser(req.params["userId"])
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
})

routes.route(`${baseUrl}/users/:userId/projects`)
.get((req, res) => {
    projectController.getAllProjects(req.params["userId"])
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.post((req, res) => {
    projectController.addProject(req.params["userId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId/projects/:projectId`)
.get((req, res) => {
    projectController.getProject(req.params["userId"], req.params["projectId"], true)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.put((req, res) => {
    projectController.updateProject(req.params["userId"], req.params["projectId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})
.delete((req, res) => {
    projectController.deleteProject(req.params["userId"], req.params["projectId"], true, true)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId/projects/:projectId/sessions`)
.post((req, res) => {
    projectController.addSession(req.params["userId"], req.params["projectId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId/projects/:projectId/sessions/:sessionId`)
.put((req, res) => {
    projectController.updateSession(req.params["userId"], req.params["projectId"], req.params["sessionId"], req.body)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})

routes.route(`${baseUrl}/users/:userId/projects/:projectId/report`)
.get((req, res) => {
    let userId: string = req.params["userId"];
    let projectId: string = req.params["projectId"];
    let from: string = req.query["from"];
    let to: string = req.query["to"];
    let includeCompletedPomodoros: boolean = req.query["includeCompletedPomodoros"]=="true";
    let includeTotalHoursWorkedOnProject: boolean = req.query["includeTotalHoursWorkedOnProject"]=="true";
    projectController.getReport(userId, projectId, from, to, includeCompletedPomodoros, includeTotalHoursWorkedOnProject)
    .then(obj => {
        res.status(obj["code"]).send(obj["result"]);
    })
    .catch(obj => {
        res.status(obj["code"]).send(obj["result"]);
    });
})